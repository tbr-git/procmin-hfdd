import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, combineLatest, forkJoin, of } from 'rxjs';
import { HFDDSession } from '../../hfdd/hfddsession';
import { HFDDSessionService } from '../hfddsession.service';

@Component({
  selector: 'app-hfdd-iteration',
  templateUrl: './hfdd-iteration.component.html',
  styleUrls: ['./hfdd-iteration.component.sass']
})
export class HfddIterationComponent implements OnInit {

  /**
   * Handle to the session
   */
  hfddSess: HFDDSession | undefined = undefined;

  /**
   * Iteration
   */
  iteration: number | undefined = undefined;

  constructor(private router: Router, private route: ActivatedRoute, private hfddSessService: HFDDSessionService) { }

  ngOnInit(): void {

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
        ////////////////////////////////////////
        // Iteration
        ////////////////////////////////////////
        this.iteration = +res.sourceIteration['iteration'];
        console.log('Done Init HFDD Iteration!');
      });
    }
    else {
      console.log('Cannot initialize HFDD Iteration Component without a parent session!');
    }
  }

}
