import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar'; 
import { ReactiveFormsModule } from '@angular/forms';
import { SessionEditorComponent } from './session-editor/session-editor.component';
import { FileuploadComponent } from './fileupload/fileupload.component';


@NgModule({
  declarations: [
    SessionEditorComponent,
    FileuploadComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    MatProgressBarModule,
    ReactiveFormsModule
  ],
  exports: [
    SessionEditorComponent
  ]
})
export class SessionsetupModule { }
