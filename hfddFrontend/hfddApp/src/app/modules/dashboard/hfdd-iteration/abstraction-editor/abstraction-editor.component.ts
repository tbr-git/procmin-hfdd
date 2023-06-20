import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import {CdkDragDrop, copyArrayItem, moveItemInArray} from '@angular/cdk/drag-drop';
import { HFDDSessionService } from '../../hfddsession.service';
import { ActivatedRoute, Router } from '@angular/router';
import { map, Subscription, switchMap } from 'rxjs';
import { UntypedFormControl } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

export type ActivityItem = {
  activityName: string;
  activityId: number;
}

@Component({
  selector: 'app-abstraction-editor',
  templateUrl: './abstraction-editor.component.html',
  styleUrls: ['./abstraction-editor.component.sass']
})
export class AbstractionEditorComponent implements OnInit, OnDestroy {

  /**
   * session id
   */
  sessId: string | undefined = undefined;

  /**
   * Abstraction type reactive form.
   */
  abstractionTypeControl = new UntypedFormControl('none');

  interVertAbstTypeControl = new UntypedFormControl('none');
 
  abstractionTypeChanges: Subscription | undefined = undefined;

  activities: ActivityItem[] = [];

  conditionActivities: ActivityItem[] = [];

  effectActivities: ActivityItem[] = [];

  conditionActivities2: ActivityItem[] = [];

  effectActivities2: ActivityItem[] = [];

  showDoubleCondition: boolean = false;

  vertexConditionActivities: ActivityItem[] = [];

  condMaxPropCoverLoss: number = 0.01;

  constructor(private router: Router, private route: ActivatedRoute, private hfddSessService: HFDDSessionService, private _snackBar: MatSnackBar) { }

  ngOnInit(): void {
    this.abstractionTypeChanges = this.abstractionTypeControl.valueChanges.subscribe(v => {
      if (v == 'freeRename') {
        this.showDoubleCondition = true;
      }
      else {
        this.showDoubleCondition = false;
      }
    });

    this.route.parent?.params
    .pipe(
      map(r => {
        this.sessId = r['sessId']
        return r['sessId']
      }),
      switchMap((sess: string) => {
        return this.hfddSessService.getCategoryMapper(sess);
      }),
      map(r => {
        return r['id2Activity'].map((v:string, i:number) => {
          const activityItem: ActivityItem = {activityName: v, activityId: i};
          return activityItem;
        })
      })
    ).subscribe(res => {
      this.activities = res;
      this.activities.sort((a1, a2) => a1.activityName.localeCompare(a2.activityName));
    });
  }

  ngOnDestroy(): void {
    this.abstractionTypeChanges?.unsubscribe()
  }

  drop(event: CdkDragDrop<ActivityItem[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      copyArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    }
  }

  applyIteration() {

    if(this.sessId === undefined) {
      this._snackBar.open("No session id!", "ok");
      return;
    }

    // Handle to the post request
    var addIteration$;
    ////////////////////////////////////////
    // Parse the vertex condition information
    ////////////////////////////////////////
    const vertexConditionType = this.interVertAbstTypeControl.value;
    var vertexConditionActIds: number[] | undefined = undefined;
    if (vertexConditionType !== 'none') {
      vertexConditionActIds = this.vertexConditionActivities.map(a => a.activityId);
    }

    ////////////////////////////////////////
    // Parse the Intra-vertex abstractions
    ////////////////////////////////////////
    const lvsAbstType = this.abstractionTypeControl.value;

    if (lvsAbstType === 'none' && vertexConditionType === 'none') {
      this._snackBar.open("Inter- and intra-vertex abstraction are none!", "ok");
      return;
    }

    if (lvsAbstType === 'none') {
      // Only inter-vertex abstraction specified
      addIteration$ = this.hfddSessService.applyVertexConditionOnly(this.sessId, vertexConditionType, vertexConditionActIds!, this.condMaxPropCoverLoss);
      console.log("Created iteration with vertex condition only: " + vertexConditionType + " - " + vertexConditionActIds);
    }
    else {
      const conditionIds = this.conditionActivities.map(a => a.activityId);
      const effectIds = this.effectActivities.map(a => a.activityId);

      if (effectIds.length == 0) {
        this._snackBar.open("Effect should not be empty!", "ok");
        return;
      }

      if (this.showDoubleCondition) {
        // Parse target ids
        const conditionIds2 = this.conditionActivities2.map(a => a.activityId);
        const effectIds2 = this.effectActivities2.map(a => a.activityId);
        
        if (vertexConditionType === 'none') {
          addIteration$ = this.hfddSessService.applyAbstractionMultiple(this.sessId, lvsAbstType, conditionIds, effectIds, conditionIds2, effectIds2, 
            vertexConditionType);
          console.log("Created LVS iteration: " + lvsAbstType);
        }
        else {
          addIteration$ = this.hfddSessService.applyAbstractionMultiple(this.sessId, lvsAbstType, conditionIds, effectIds, conditionIds2, effectIds2, 
            vertexConditionType, vertexConditionActIds, this.condMaxPropCoverLoss);
          console.log("Created LVS iteration " + lvsAbstType + "and vertex condition: " + vertexConditionType + " - " + vertexConditionActIds);
        }
      }
      else {
        if (vertexConditionType === 'none') {
          addIteration$ = this.hfddSessService.applyAbstractionSingle(this.sessId, this.abstractionTypeControl.value, conditionIds, effectIds);
          console.log("Created LVS iteration: " + lvsAbstType);
        }
        else {
          addIteration$ = this.hfddSessService.applyAbstractionSingle(this.sessId, this.abstractionTypeControl.value, conditionIds, effectIds,
            vertexConditionType, vertexConditionActIds, this.condMaxPropCoverLoss);
            console.log("Created LVS iteration " + lvsAbstType + "and vertex condition: " + vertexConditionType + " - " + vertexConditionActIds);
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // POST Request And Subscribe
    ////////////////////////////////////////////////////////////////////////////////
    addIteration$.subscribe({
      next: (v) => console.log(v),
      error: (e) => console.error(e),
      complete: () => console.info('complete') 
    });
  }

 /** Predicate function that doesn't allow items to be dropped into a list. */
  noReturnPredicate() {
    return false;
  }

  deleteEffect(activityItem: ActivityItem) {
    this.effectActivities.splice(this.effectActivities.indexOf(activityItem), 1);
  }

  deleteEffect2(activityItem: ActivityItem) {
    this.effectActivities2.splice(this.effectActivities2.indexOf(activityItem), 1);
  }

  deleteCondition(activityItem: ActivityItem) {
    this.conditionActivities.splice(this.conditionActivities.indexOf(activityItem), 1);
  }

  deleteCondition2(activityItem: ActivityItem) {
    this.conditionActivities2.splice(this.conditionActivities2.indexOf(activityItem), 1);
  }

  deleteVertexCondition(activityItem: ActivityItem) {
    this.vertexConditionActivities.splice(this.vertexConditionActivities.indexOf(activityItem), 1);
  }
}