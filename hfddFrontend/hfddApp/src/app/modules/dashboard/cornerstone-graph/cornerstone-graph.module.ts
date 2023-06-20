import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CornerstoneGraphComponent } from './cornerstone-graph.component';
import { RouterModule } from '@angular/router';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CornerstoneGraphDdgComponent } from './cornerstone-graph-ddg/cornerstone-graph-ddg.component';
import { CornerstoneGraphEditComponent } from './cornerstone-graph-edit/cornerstone-graph-edit.component';
import { MatList, MatListModule } from '@angular/material/list';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatSnackBarModule } from '@angular/material/snack-bar';



@NgModule({
  declarations: [
    CornerstoneGraphComponent,
    CornerstoneGraphDdgComponent,
    CornerstoneGraphEditComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    MatButtonToggleModule,
    ReactiveFormsModule,
    MatListModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    MatButtonModule,
    DragDropModule,
    MatSnackBarModule,
  ]
})
export class CornerstoneGraphModule { }
