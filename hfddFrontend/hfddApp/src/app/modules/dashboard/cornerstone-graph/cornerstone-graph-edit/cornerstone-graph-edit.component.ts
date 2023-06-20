import { AfterViewInit, Component, OnInit } from '@angular/core';
import { HFDDSessionService } from '../../hfddsession.service';
import { HFDDSession } from 'src/app/modules/hfdd/hfddsession';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, combineLatest, map, switchMap, throwError, withLatestFrom } from 'rxjs';
import { error } from 'console';
import { ActivityItem } from '../../hfdd-iteration/abstraction-editor/abstraction-editor.component';
import { CdkDragDrop, copyArrayItem, moveItemInArray } from '@angular/cdk/drag-drop';
import { MatSnackBar } from '@angular/material/snack-bar';

export type VertexActivityInfo = {
  id: number;
  activities: string[];
}

@Component({
  selector: 'app-cornerstone-graph-edit',
  templateUrl: './cornerstone-graph-edit.component.html',
  styleUrls: ['./cornerstone-graph-edit.component.sass']
})
export class CornerstoneGraphEditComponent implements OnInit, AfterViewInit {

  /**
   * Handle to the session
   */
  hfddSess: HFDDSession | undefined = undefined;

  vertexActivityInfo: VertexActivityInfo[] | undefined;

  activities: ActivityItem[] = [];

  addCSActivities: ActivityItem[] = [];

  constructor(private router: Router, private route: ActivatedRoute, private hfddSessService: HFDDSessionService,  
    private _snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.route.parent?.params
    if (this.route.parent) {
      combineLatest({
        sourceSession: this.route.parent?.params,
        sourceIteration: this.route.params
      })
      .subscribe(res => {
        ////////////////////////////////////////
        // Get handle to session 
        ////////////////////////////////////////
        const sessId = res.sourceSession['sessId'];

        let hfddSessUnresolved = this.hfddSessService.getSessionById(sessId);

        if (hfddSessUnresolved) {
          this.hfddSess = hfddSessUnresolved;
        }
        else{
          console.log("Problem resolving the session");
        }
      });
    }
    else {
      console.log('Cannot initialize HFDD Iteration Component without a parent session!');
    }
  }

  ngAfterViewInit(): void {
    const hfddSess$ = this.route.parent?.params
      .pipe(
        switchMap(params => {
          let hfddSessUnresolved = this.hfddSessService.getSessionById(params['sessId']);
          if (hfddSessUnresolved) {
            this.hfddSess = hfddSessUnresolved
            return new BehaviorSubject(hfddSessUnresolved);
          }
          else {
            return throwError(() => Error("session undefined"));
          }
        }),
      );
    const csChanges$ = 
        hfddSess$?.pipe(
          switchMap((sess:HFDDSession) => sess.cornerstoneSpecSubject$)
        );

    const activityTranslation$ = 
        hfddSess$?.pipe(
          switchMap((sess:HFDDSession) => sess.cornerstoneSpecSubject$),
          withLatestFrom(hfddSess$),
          switchMap(([csSpec, sess]) => this.hfddSessService.getActivitiesForVertices(sess, csSpec.cornerstoneVertices))
        )
      .subscribe(
        {
          next: (v: VertexActivityInfo[]) => {
            this.vertexActivityInfo = v;
          },
          error: (e) => console.error(e)
        });

    hfddSess$?.pipe(
      switchMap((sess: HFDDSession) => {
          return this.hfddSessService.getCategoryMapper(sess.sessId);
        }),
        map(r => {
          return r['id2Activity'].map((v:string, i:number) => {
            const activityItem: ActivityItem = {activityName: v, activityId: i};
            return activityItem;
          })
        })
      )
      .subscribe(res => {
        this.activities = res;
        this.activities.sort((a1, a2) => a1.activityName.localeCompare(a2.activityName));
      });
  }

  deleteCS(vertexId: number) {
    this.hfddSess?.removeCornerstoneVertex(vertexId);
  }

  deleteAddCSActivity(activityItem: ActivityItem) {
    this.addCSActivities.splice(this.addCSActivities.indexOf(activityItem), 1);
  }

  noReturnPredicate() {
    return false;
  }
  
  drop(event: CdkDragDrop<ActivityItem[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      copyArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    }
  }

  addAddCSActivity() {
    if (this.hfddSess && this.addCSActivities.length > 0) {
      this.hfddSessService.getVertexForActivities(this.hfddSess, this.addCSActivities.map(a => a.activityId))
        .subscribe({
          next: (v: number|null) =>  {
            if (v !== null) {
              console.log("Add additional CS vertex");
              this.hfddSess?.addCornerstoneVertex(v);
            }
            else {
              this._snackBar.open("This vertex does not exist!", "ok");
              console.log("Vertex does not exist!");
            }
          }
        })

    }
  }

}
