import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { HFDDSession } from 'src/app/modules/hfdd/hfddsession';
import { HFDDSessionService } from '../../../hfddsession.service';
import { catchError, map, of } from 'rxjs';
import { DiffCandidateInfo } from 'src/app/modules/hfdd/data/diff-candidate-info';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-dominating-vertices',
  templateUrl: './dominating-vertices.component.html',
  styleUrls: ['./dominating-vertices.component.sass']
})
export class DominatingVerticesComponent implements OnInit {

  @Output()
  private itemsetSelectedEvent = new EventEmitter<DiffCandidateInfo>();

  /**
   * Form group for loading dominating vertices
   */
  domCompOptions: UntypedFormGroup;

  /**
   * Control: Metric Threshold otherwise not considered
   */
  metricThresholdControl = new UntypedFormControl(0, Validators.min(0));

  /**
   * Control: Metric surprise factor
   */
  metricSurpriseControl = new UntypedFormControl(1.3, [Validators.min(0), Validators.max(1)]);

  /**
   * Control: Domination propagation factor
   */
  metricBwdDomControl = new UntypedFormControl(0.95, [Validators.min(0), Validators.max(1)]);

  /**
   * List of retrieved vertices
   */
  dominatingVertices: DiffCandidateInfo[] = [];

  /**
   * Is loading dominating vertices
   */
  isLoadingVertices = false;

  displayedColumns: string[] = ['activities', 'metric', 'condactivities'];

  /**
   * Currently selected Vertex
   */
  selectedVertex?: DiffCandidateInfo;


  @Input() 
  hfddSession?: HFDDSession;

  @Input()
  iteration?: number;

  constructor(private hfddService: HFDDSessionService, private _snackBar: MatSnackBar, fb: UntypedFormBuilder) {
    this.domCompOptions = fb.group({
      metricThreshold: this.metricThresholdControl,
      metricSurprise: this.metricSurpriseControl,
      bwdDom: this.metricBwdDomControl
    });
   }


  ngOnInit(): void {
  }

  onCaculateDominatingVertices(): void {
    // TODO Switch map for multiclicking
    //@ViewChild('button') button;
    //clicks$:Observable<any>;

    //ngOnInit() {
     // this.clicks$ = Observable.fromEvent(this.button.nativeElement, 'click');
    //}
    if (this.hfddSession && this.iteration !== undefined) {
      this.isLoadingVertices = true;
      this.hfddService.getDominatingVertices(this.hfddSession.sessId, this.iteration, 
          this.metricThresholdControl.value, this.metricSurpriseControl.value, this.metricBwdDomControl.value)
        .pipe(
          catchError(() => of(null)),
          map(data => {
            this.isLoadingVertices = false;
            if (data === null) {
              return []
            }
            else {
              return data;
            }
          }),
          map(data => {
              data.sort((u, v) => v.metric - u.metric)
              return data;
          })
        )
        .subscribe(data => this.dominatingVertices = data);
    }
  }

  onItemsetSelected(vertex: DiffCandidateInfo) : void {
    this.selectedVertex = vertex;
    this.itemsetSelectedEvent.emit(vertex);
  }

  addCornerstone() {
    this.hfddSession?.addCornerstoneVertex(this.selectedVertex?.id);
  }

  addCornerstoneWithContext() {
    if (this.selectedVertex !== undefined) {
      if (this.selectedVertex.idCondActUnion > -1) {
        this.hfddSession?.addCornerstoneVertex(this.selectedVertex.idCondActUnion);
      }
      else {
        this._snackBar.open('Conditioned Union Subset does not exist in backend! Cannot add it.');
      }
    }

  }
}
