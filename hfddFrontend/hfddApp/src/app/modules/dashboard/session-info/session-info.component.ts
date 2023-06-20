import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HFDDSession } from '../../hfdd/hfddsession';
import { HFDDSessionService } from '../hfddsession.service';

@Component({
  selector: 'app-session-info',
  templateUrl: './session-info.component.html',
  styleUrls: ['./session-info.component.sass']
})
export class SessionInfoComponent implements OnInit {

  hfddSess: HFDDSession | undefined = undefined;

  sessInfo: string | undefined = undefined;

  constructor(private route: ActivatedRoute, private hfddSessService: HFDDSessionService) { }

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      const sessId = params['sessId'];

      let hfddSessUnresolved = this.hfddSessService.getSessionById(sessId);

      if (hfddSessUnresolved) {
        this.hfddSess = hfddSessUnresolved;
        this.sessInfo = this.hfddSess.sessDescription;
      }
    })
  }

}
