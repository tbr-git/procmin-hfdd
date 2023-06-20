import { Component, OnInit } from '@angular/core';
import { HFDDSessionService } from '../../dashboard/hfddsession.service';
import { HFDDSession } from '../../hfdd/hfddsession';
import { Router } from '@angular/router';

@Component({
  selector: 'app-session-overview',
  templateUrl: './session-overview.component.html',
  styleUrls: ['./session-overview.component.sass']
})
export class SessionOverviewComponent implements OnInit {

  /**
   * List of sessions.
   */
  sessionList: HFDDSession[] | undefined;
  

  displayedColumns: string[] = ['sessName', 'sessDesc'];

  constructor(private router: Router, private hfddService: HFDDSessionService) {
   }

  ngOnInit(): void {
    this.hfddService.getSessions().subscribe({
      next: itSessions => {
        this.sessionList = Array.from(itSessions);
        console.log("Session List");
        console.log(this.sessionList);
        console.log(itSessions);
        if (this.sessionList.length === 0) {
          this.sessionList = undefined;
        }
      }
    })
  }

  onSessionSelected(sess: HFDDSession) : void {
    this.router.navigate(['/comparisonSession', sess.sessId]);
  }

}
