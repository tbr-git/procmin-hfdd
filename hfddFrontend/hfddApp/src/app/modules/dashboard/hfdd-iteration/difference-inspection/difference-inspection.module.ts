import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MetricOverviewComponent } from './metric-overview/metric-overview.component';
import { MatCardModule } from '@angular/material/card';
import { DominatingVerticesComponent } from './dominating-vertices/dominating-vertices.component';
import { MatFormFieldModule } from '@angular/material/form-field'; 
import { MatInputModule } from '@angular/material/input'; 
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'; 
import { ReactiveFormsModule } from '@angular/forms';
import { ItemsetDifferenceViewComponent } from './itemset-difference-view/itemset-difference-view.component';
import { DifferenceInspectionComponent } from './difference-inspection.component';
import { DiffDdgComponent } from './diff-ddg/diff-ddg.component';

@NgModule({
  declarations: [
    DifferenceInspectionComponent,
    MetricOverviewComponent,
    DominatingVerticesComponent,
    ItemsetDifferenceViewComponent,
    DiffDdgComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatProgressSpinnerModule,
  ],
  exports: [
    DifferenceInspectionComponent
  ]
})
export class DifferenceInspectionModule { }
