import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DifferenceInspectionModule } from './difference-inspection/difference-inspection.module';
import { HfddIterationComponent } from './hfdd-iteration.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { AbstractionEditorComponent } from './abstraction-editor/abstraction-editor.component';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle'; 
import { MatIconModule } from '@angular/material/icon';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatExpansionModule } from '@angular/material/expansion';



@NgModule({
  declarations: [
    HfddIterationComponent,
    AbstractionEditorComponent
  ],
  imports: [
    CommonModule,
    DifferenceInspectionModule,
    DragDropModule,
    MatButtonModule,
    MatIconModule,
    MatButtonToggleModule,
    ReactiveFormsModule,
    MatSnackBarModule,
    MatExpansionModule
  ]
})
export class HfddIterationModule { }
