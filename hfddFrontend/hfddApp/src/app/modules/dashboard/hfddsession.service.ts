import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable, concat, forkJoin, of, throwError } from 'rxjs';
import { concatMap, last, map, shareReplay } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { CSGraphSpec } from '../hfdd/cs-graph-spec';
import { DiffCandidateInfo } from '../hfdd/data/diff-candidate-info';
import { PDFGQueryResult } from '../hfdd/data/pddf-graph/PDFG-query-result';
import { ResponseMessage } from '../hfdd/data/util/ResponseMessage';
import { VertexMeasurement } from '../hfdd/data/vertex-measurement';
import { HFDDSession } from '../hfdd/hfddsession';
import { VertexActivityInfo } from './cornerstone-graph/cornerstone-graph-edit/cornerstone-graph-edit.component';


//type InitializationInfo [Observable<HFDDSession>, // The session
//  Observable<any>,  // Upload left logs (successs string + upload events)
//  Observable<any>,  // Upload right logs (successs string + upload events)
//  Observable<any>]; // Session initialization

@Injectable({
  providedIn: 'root'
})
export class HFDDSessionService {

  /**
   * Backend URL
   */
  private hfddUrl = environment.production ? '/api' : 'http://137.226.117.2:8082';

  /**
   * Standard header options
   */
  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  /**
   * Session repository
   */
  private sessionMap = new Map<string, HFDDSession>();

  constructor(
    private http: HttpClient) { 
      console.log("Creating new session service");

    }

  createSession(sessName: string, sessDescription: string, logLeft:File, logRight:File,
        spsMiningTime: number|undefined,
        spsTargetSize: number|undefined,
        spsTargetSizeMargin: number|undefined,
        maxUnroll: number|undefined): [Observable<HFDDSession>, Observable<any>, Observable<any>, Observable<string>] {
    // Session creation observable
    const sess$ = this.http.post<string>(this.hfddUrl + '/createRun', this.httpOptions)
      .pipe(
        map(res => {
          const sess: HFDDSession = new HFDDSession(res, sessName, sessDescription);
          return sess;
        }),
        shareReplay(1)
      );
    // Subscribe and add session to session repository
    sess$.subscribe(
      {
        next: hfddSess => {
          console.log('Next session');
          console.log(hfddSess);
          this.sessionMap.set(hfddSess.sessId, hfddSess);
          console.log(this.sessionMap);
        }
      });

    const uploadLeft$ = sess$.pipe(
      last(),
      concatMap(hfddSess => this.uploadLog(`${hfddSess.sessId}/logLeft`, "logLeft", logLeft)),
      shareReplay(1)
      );

    const uploadRight$ = sess$.pipe(last(),
      concatMap(hfddSess => this.uploadLog(`${hfddSess.sessId}/logRight`, "logRight", logRight)),
      shareReplay(1)
      );

    let params = new HttpParams()
      .set("freqActMiningTimeMs", spsMiningTime ?? 10000)
      .set("targetActISNbr", spsTargetSize ?? 1000)
      .set("targetActISMargin", spsTargetSizeMargin ?? 0.1)
      .set("maxLoopUnroll", maxUnroll ?? -1);

    const initComparison$ = forkJoin([sess$, uploadLeft$, uploadRight$]).pipe(
      //concatMap((hfddSess: any, resUl:any, resUr:any) =>
      concatMap(res =>
        concat([of("Start initialization"), this.http.post<string>(this.hfddUrl + `/${res[0].sessId}/initComparisonISSearch`, this.httpOptions, {params: params}), of("Stop initialization")])
        .pipe(concatMap(o => o))
      ),
      shareReplay(1)
    );

    return [sess$, uploadLeft$, uploadRight$, initComparison$];
  }

  uploadLog(endpoint: string, paramName: string, file:File): Observable<any> {
    if (file) {
      const formData = new FormData();
      formData.append(paramName, file);
      return this.http.post<any>(this.hfddUrl + `/${endpoint}`, formData, {
        reportProgress: true,
        observe: 'events'
      });
    }
    throw throwError(() => Error("Not a valid file"));
  }

  getSessionById(sessId: string) : HFDDSession | undefined {
    //console.log(`Size session map ${this.sessionMap.size}`);
    //this.sessionMap.forEach(e => console.log(e));
    const hfddSess = this.sessionMap.get(sessId)
    return hfddSess;
  }

  getMetrics(sessId: string, iteration: number): Observable<VertexMeasurement[]> {
    const vertexMeasurements$ = this.http.get<VertexMeasurement[]>(this.hfddUrl + `/${sessId}/${iteration}/getMetricValues`, this.httpOptions);
    return vertexMeasurements$;
  }

  getDominatingVertices(sessId: string, iteration: number, metricThreshold: number,
      metricSurpriseThreshold: number, metricDomThreshold: number): Observable<DiffCandidateInfo[]> {
    const domVertices$ = this.http.post<DiffCandidateInfo[]>(this.hfddUrl + `/${sessId}/${iteration}/getDomVertices`,
      {
        metricThreshold: metricThreshold,
        metricSurpriseThreshold: metricSurpriseThreshold,
        backwardDominationThreshold: metricDomThreshold
      }, this.httpOptions);
    return domVertices$;
  }

  getIntraItemsetSankey(sessId: string, iteration: number, vertexId: number) : Observable<any> {
    return this.http.get<any>(this.hfddUrl + `/${sessId}/${iteration}/getIntraVertexFlow/${vertexId}`, this.httpOptions);
  }

  getCategoryMapper(sessId: string): Observable<any> {
    return this.http.get<any>(this.hfddUrl + `/${sessId}/getCatMapper`, this.httpOptions);
  }

  applyVertexConditionOnly(sessId: string, vertAbstType: string, vertexActIds: number[], condMaxPropCoverLoss: number): Observable<any> {
    const hfddSess : HFDDSession | undefined = this.getSessionById(sessId);
    ////////////////////////////////////////////////////////////////////////////////
    // Basic Validity Checks
    ////////////////////////////////////////////////////////////////////////////////
    // Valid Session
    if (hfddSess === undefined) {
      return throwError(() => new Error("Invalid spec!"));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Post vertex condition (with empty LVS Abstraction)
    ////////////////////////////////////////////////////////////////////////////////
    const vertexCondLVSAbstraction = {
        abstractions: [],
        condVertex: vertexActIds,
        condMaxPropCoverLoss: condMaxPropCoverLoss,
        vertCondType: vertAbstType
      };

    const postIterationSpec$ = this.http.post<ResponseMessage>(this.hfddUrl + `/${sessId}/addIterationCond`, vertexCondLVSAbstraction, this.httpOptions)
    return this.actOnBackendIterationCompletion(hfddSess, postIterationSpec$);

  }

  applyAbstractionSingle(sessId: string, abstractionType: string, conditionIds: number[], effectIds: number[],
      vertAbstType?: string, vertexActIds?: number[], condMaxPropCoverLoss?: number): Observable<any> {
    const hfddSess : HFDDSession | undefined = this.getSessionById(sessId);
    ////////////////////////////////////////////////////////////////////////////////
    // Basic Validity Checks
    ////////////////////////////////////////////////////////////////////////////////
    // Valid Session
    if (hfddSess === undefined) {
      return throwError(() => new Error("Invalid spec!"));
    }
    // Transform type name -> or throw error
    if (abstractionType == "freeDelete") {
      abstractionType = "FREEDELETE";
    }
    else if (abstractionType == "freeInsert") {
      abstractionType = "FREEINSERT";
    }
    else {
      return throwError(() => new Error("Invalid Abstraction Specification (type)!"));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Create LVS Abstraction
    ////////////////////////////////////////////////////////////////////////////////
    const abstractionSpec = {
      abstractionType: abstractionType,
      conditionActivities: conditionIds,
      affectedActivities: effectIds,
      abstractionID:  hfddSess.maxIteration
    };
    const comparisonAbstraction = [{
      abstractionLeft: (abstractionType == "FREEDELETE") ? abstractionSpec : null,
      abstractionRight: (abstractionType == "FREEINSERT") ? abstractionSpec : null,
      leftRightAbstractionEqual: false
    }];

    ////////////////////////////////////////////////////////////////////////////////
    // If provided, integrate the vertex-based conditioning
    // AND POST
    ////////////////////////////////////////////////////////////////////////////////
    var postIterationSpec$;
    if (vertAbstType !== undefined && vertAbstType !== 'none') {
      const vertexCondLVSAbstraction = {
        abstractions: comparisonAbstraction,
        condVertex: vertexActIds,
        condMaxPropCoverLoss: condMaxPropCoverLoss,
        vertCondType: vertAbstType
      };
      postIterationSpec$ = this.http.post<ResponseMessage>(this.hfddUrl + `/${sessId}/addIterationCond`, vertexCondLVSAbstraction, this.httpOptions)
    }
    else {
      postIterationSpec$ = this.http.post<ResponseMessage>(this.hfddUrl + `/${sessId}/addIteration`, comparisonAbstraction, this.httpOptions)
    }

    return this.actOnBackendIterationCompletion(hfddSess, postIterationSpec$);
  }

  applyAbstractionMultiple(sessId: string, lvsAbstType: string,
      conditionIdsSource: number[], effectIdsSource: number[],
      conditionIdsTarget: number[], effectIdsTarget: number[],
      vertAbstType?: string, vertexActIds?: number[], condMaxPropCoverLoss?: number): Observable<any> {

    const hfddSess : HFDDSession | undefined = this.getSessionById(sessId);
    ////////////////////////////////////////////////////////////////////////////////
    // Basic Validity Checks
    ////////////////////////////////////////////////////////////////////////////////
    // Valid Session
    if (hfddSess === undefined) {
      return throwError(() => new Error("Invalid spec!"));
    }
    // Transform type name -> or throw error
    if (lvsAbstType == "freeRename") {
      lvsAbstType = "FREERENAME";
    }
    else {
      return throwError(() => new Error("Invalid Abstraction Specification (type)!"));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Create LVS Abstraction
    ////////////////////////////////////////////////////////////////////////////////
    const abstractionSpec = [{
      abstractionLeft: {
        abstractionType: lvsAbstType,
        conditionActivities: conditionIdsSource,
        affectedActivities: effectIdsSource,
        abstractionID: hfddSess.maxIteration
      },
      abstractionRight: {
        abstractionType: lvsAbstType,
        conditionActivities: conditionIdsTarget,
        affectedActivities: effectIdsTarget,
        abstractionID: -1 * hfddSess.maxIteration
      },
      leftRightAbstractionEqual: false
    }];

    ////////////////////////////////////////////////////////////////////////////////
    // If provided, integrate the vertex-based conditioning
    // AND POST
    ////////////////////////////////////////////////////////////////////////////////
    var postIterationSpec$;
    if (vertAbstType !== undefined && vertAbstType !== 'none') {
      const vertexCondLVSAbstraction = {
        abstractions: abstractionSpec,
        condVertex: vertexActIds,
        condMaxPropCoverLoss: condMaxPropCoverLoss,
        vertCondType: vertAbstType
      };
      postIterationSpec$ = this.http.post<ResponseMessage>(this.hfddUrl + `/${sessId}/addIterationCond`, vertexCondLVSAbstraction, this.httpOptions)
    }
    else {
      postIterationSpec$ = this.http.post<ResponseMessage>(this.hfddUrl + `/${sessId}/addIteration`, abstractionSpec, this.httpOptions)
    }

    return this.actOnBackendIterationCompletion(hfddSess, postIterationSpec$);
  }

  getCornerstoneSankey(sessId: string, conerstoneVertices: number[]) : Observable<any> {
    return this.http.post<any>(this.hfddUrl + `/${sessId}/getCornerstoneGraph`, conerstoneVertices, this.httpOptions);
  }

  getDDGraph(sessId: string, csSpec: CSGraphSpec) : Observable<any> {
    // If the iteration is not specificied, send "-1" to avoid problems in serialization
    console.log("Get cornerstone graph spec:");
    console.log(csSpec);
    let csSpecSend: CSGraphSpec = Object.assign({}, csSpec);
    csSpecSend.conditionIteration = csSpecSend.conditionIteration ?? -1;
    console.log("Send cornerstone graph spec:");
    console.log(csSpecSend);
    return this.http.post<any>(this.hfddUrl + `/${sessId}/getDDGraph`, csSpecSend, this.httpOptions);
  }

  getDiffDDG(sessId: string, iteration: number, vertexId: number) : Observable<PDFGQueryResult> {
    return this.http.get<any>(this.hfddUrl + `/${sessId}/${iteration}/getIntraVertexDFG/${vertexId}`, this.httpOptions);
      //.pipe(
      //  map(r => {
      //    console.log(r);
      //    let vertex_map = new Map<string, PDFGVertex>();
      //    r.diffDFG.vertices.forEach((v: any) => vertex_map.set(v.categoryCode.toString(), v));
      //    let edge_map = new Map<string, PDFGEdge>();
      //    r.diffDFG.edges.forEach((e: any) => edge_map.set(e.id.toString(), e));
      //    return {diffDFG: {vertices: vertex_map, edges: edge_map}, dotString: r.dotString}
      //  })
      //);
  }

  actOnBackendIterationCompletion(hfddSess: HFDDSession, postIterationSpec$: Observable<any> ) : Observable<any> {
    let processedIterationSpecPost$ = postIterationSpec$
      .pipe(
        last(),
        map(v => {
          hfddSess.incIteration()
          return v;
        })
      );

    return processedIterationSpecPost$;
  }

  dropLastIteration(hfddSess: HFDDSession) {
      const sessId: string = hfddSess.sessId;
      return this.http.post<any>(this.hfddUrl + `/${sessId}/dropLastIteration`, {}, this.httpOptions);
  }

  getActivitiesForVertices(hfddSess: HFDDSession, vertexIds: number[]): Observable<VertexActivityInfo[]> {
    return this.http.post<VertexActivityInfo[]>(
      this.hfddUrl + `/${hfddSess.sessId}/getActivitiesForIds`, vertexIds, this.httpOptions);
  }

  getVertexForActivities(hfddSess: HFDDSession, activityIds: number[]): Observable<number|null> {
    return this.http.post<number|null>(
      this.hfddUrl + `/${hfddSess.sessId}/getVertexForActivities`, activityIds, this.httpOptions);
  }

  getSessions(): Observable<Iterable<HFDDSession>> {
    console.log(this.sessionMap);
    return of(this.sessionMap.values());
  }

}
