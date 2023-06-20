import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DefaultComponent } from './default.component';
import { RouterModule } from '@angular/router';
import { SharedModule } from 'src/app/shared/shared.module';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SessionsetupModule } from 'src/app/modules/sessionsetup/sessionsetup.module';



@NgModule({
  declarations: [
    DefaultComponent,
  ]
  ,
  imports: [
    CommonModule,
    RouterModule,
    SharedModule,
    SessionsetupModule,
    MatSidenavModule,
    MatToolbarModule
  ]
})
export class DefaultModule { }
