import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DefaultComponent } from './layouts/default/default.component';
import { CornerstoneGraphDdgComponent } from './modules/dashboard/cornerstone-graph/cornerstone-graph-ddg/cornerstone-graph-ddg.component';
import { DashboardComponent } from './modules/dashboard/dashboard.component';
import { AbstractionEditorComponent } from './modules/dashboard/hfdd-iteration/abstraction-editor/abstraction-editor.component';
import { HfddIterationComponent } from './modules/dashboard/hfdd-iteration/hfdd-iteration.component';
import { SessionInfoComponent } from './modules/dashboard/session-info/session-info.component';
import { SessionEditorComponent } from './modules/sessionsetup/session-editor/session-editor.component';
import { CornerstoneGraphEditComponent } from './modules/dashboard/cornerstone-graph/cornerstone-graph-edit/cornerstone-graph-edit.component';
import { SessionOverviewComponent } from './modules/session-management/session-overview/session-overview.component';

const routes: Routes = [{
  path: '',
  component: DefaultComponent,
  children: [
    {
      path: '',
      redirectTo: 'sessions',
      pathMatch: 'full'
    },    {
      path: 'sessions',
      component: SessionOverviewComponent
    },
    {
      path: 'testDDG',
      component: CornerstoneGraphDdgComponent
    },
    {
      path: 'createSession',
      component: SessionEditorComponent
    },
    {
      path: 'comparisonSession/:sessId',
      component: DashboardComponent,
      children: [
        {
          path: '',
          redirectTo: 'info',
          pathMatch: 'full'
        },
        {
          path: 'info',
          component: SessionInfoComponent
        },
        {
          path: 'iteration/:iteration',
          component: HfddIterationComponent
        },
        {
          path: 'addAbstraction',
          component: AbstractionEditorComponent
        },
        {
          path: 'csGraphEdit',
          component: CornerstoneGraphEditComponent
        },
        {
          path: 'csGraph',
          component: CornerstoneGraphDdgComponent
        }

      ]
    }
  ]
}];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
