import { Component, OnInit } from '@angular/core';
import { HFDDSessionService } from 'src/app/modules/dashboard/hfddsession.service';

@Component({
  selector: 'app-default',
  templateUrl: './default.component.html',
  styleUrls: ['./default.component.sass']
})
export class DefaultComponent implements OnInit {

  sideBarOpen = true;

  constructor(private hfddSessService : HFDDSessionService) { }

  ngOnInit(): void {
  }

  sideBarToggler() {
    this.sideBarOpen = !this.sideBarOpen;
  }

}
