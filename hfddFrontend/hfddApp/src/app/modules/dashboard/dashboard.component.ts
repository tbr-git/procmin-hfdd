import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { catchError, Observable, switchMap, throwError } from 'rxjs';
import { HFDDSession } from '../hfdd/hfddsession';
import { HFDDSessionService } from './hfddsession.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.sass']
})
export class DashboardComponent implements OnInit {

  hfddSess: HFDDSession | undefined = undefined;

  iterations: number[] | undefined = undefined;

  sessIteration$: Observable<number> | undefined = undefined;

  constructor(private router: Router, private route: ActivatedRoute, private hfddSessService: HFDDSessionService) { }

  ngOnInit(): void {
    console.log("Hello from Dashboard!")
    this.sessIteration$ = this.route.params.pipe(
      switchMap(p => {
        const sessId = p['sessId']
        let hfddSessUnresolved = this.hfddSessService.getSessionById(sessId);
        
        if (hfddSessUnresolved == undefined) {
          console.log('Invalid session');
          this.router.navigate(['createSession']);
          throw throwError(() => "Invalid Session");
        }
        else {
          this.hfddSess = hfddSessUnresolved;
          return this.hfddSess.getIterationUpdates()
        }
        })
    );
    this.sessIteration$
      .subscribe(
        {
          next: (i) => {
            console.log(`New iteration ${i}`);
            this.iterations = Array.from(Array(i + 1).keys());
          },
          error: (e) => console.error(e)
        });


    //this.route.params.subscribe(p => {
    //  console.log(p);
    //  const sessId = p['sessId']
    //  let hfddSessUnresolved = this.hfddSessService.getSessionById(sessId);
    //  
    //  if (hfddSessUnresolved == undefined) {
    //    console.log('Invalid session');
    //   console.log(sessId);
    //    this.router.navigate(['createSession']);
    //  }
    //  else {
    //    this.hfddSess = hfddSessUnresolved;
    //    this.iterations = Array.from(Array(this.hfddSess.maxIteration + 1).keys());
    //  }
    //});
  }

  dropLastIteration() : void {
    console.log("Clicked Drop");
    if (this.hfddSess !== undefined && ((this.iterations?.length ?? 0) > 1)) {
      const dropRequest$ : Observable<any> = this.hfddSessService.dropLastIteration(this.hfddSess);
      dropRequest$.subscribe({
        next: (r) => {
          this.hfddSess?.decIteration();
        },
        error: (e) => console.error(e)
      })
      // TODO remove last iteration from list if sucessfull
    }
    else {
      console.log("Session undefined");
    }

  }

}
