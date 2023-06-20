import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SessionOverviewComponent } from './session-overview/session-overview.component';
import { MatTableModule } from '@angular/material/table';
import { MatSidenavModule } from '@angular/material/sidenav';



@NgModule({
  declarations: [
    SessionOverviewComponent
  ],
  imports: [
    CommonModule,
    MatTableModule,
    MatSidenavModule,
  ],
  exports: [
    SessionOverviewComponent
  ]
})
export class SessionManagementModule { }
