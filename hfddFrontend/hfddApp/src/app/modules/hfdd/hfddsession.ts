import { BehaviorSubject, Observable } from "rxjs";
import { CSGraphSpec } from "./cs-graph-spec";

export class HFDDSession {
    /**
     * Session Id
     */
    readonly sessId: string;

    /**
     * Session Name
     */
    sessName: string;

    /**
     * Session description
     */
    sessDescription: string;

    /**
     * Number of applied iterations (initially 0)
     */
    _maxIteration: number;

    iteration$: BehaviorSubject<number>;

    cornerstoneSpec: CSGraphSpec;

    cornerstoneSpecSubject$: BehaviorSubject<CSGraphSpec>;

    /**
     * 
     * @param sessId 
     * @param sessName 
     * @param sessDescription 
     */
    constructor(sessId: string, sessName: string, sessDescription: string) {
        this.sessId = sessId;
        this.sessName = sessName;
        this.sessDescription = sessDescription;
        this._maxIteration = 0;

        this.iteration$ = new BehaviorSubject(0);
        this.cornerstoneSpec = {conditionIteration: undefined, cornerstoneVertices: new Array<number>()};
        this.cornerstoneSpecSubject$ = new BehaviorSubject(this.cornerstoneSpec);
    }

    incIteration() {
        console.log("Incrementing the iteration.")
        this._maxIteration++;
        this.iteration$.next(this._maxIteration);
    }

    decIteration() {
        this._maxIteration--;
        this.iteration$.next(this._maxIteration);
    }

    get maxIteration() {
        return this._maxIteration;
    }

    getIterationUpdates() {
        return this.iteration$;
    }

    addCornerstoneVertex(vertexId: number|undefined) {
        if (vertexId != undefined) {
            if (this.cornerstoneSpec.cornerstoneVertices.indexOf(vertexId) == -1) {
                this.cornerstoneSpec.cornerstoneVertices.push(vertexId);
                this.cornerstoneSpecSubject$.next(this.cornerstoneSpec);
                console.log(this.cornerstoneSpec);
            }
        }
    }

    removeCornerstoneVertex(vertexId: number | undefined) {
        if (vertexId != undefined) {
            let i = this.cornerstoneSpec.cornerstoneVertices.indexOf(vertexId)
            if (i > -1) {
                this.cornerstoneSpec.cornerstoneVertices.splice(i, 1);
                this.cornerstoneSpecSubject$.next(this.cornerstoneSpec);
            }
        }
    }

    setCornerstoneConditionIteration(iteration: number | undefined) {
        this.cornerstoneSpec.conditionIteration = iteration;
        this.cornerstoneSpecSubject$.next(this.cornerstoneSpec);
    }
}