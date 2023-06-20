import { AfterViewInit, Component, ElementRef, Input, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { HFDDSession } from '../../../../hfdd/hfddsession';
import { HFDDSessionService } from '../../../hfddsession.service';
import { Observable, last, map } from 'rxjs';
import { Graphviz, graphviz } from 'd3-graphviz';
import * as d3 from 'd3';
import { PDFGQueryResult } from 'src/app/modules/hfdd/data/pddf-graph/PDFG-query-result';

@Component({
  selector: 'app-diff-ddg',
  templateUrl: './diff-ddg.component.html',
  styleUrls: ['./diff-ddg.component.sass']
})
export class DiffDdgComponent implements AfterViewInit {

  /**
   * Handle to current session
   */
  @Input() hfddSession: HFDDSession | undefined = undefined;

  /**
   * Current HFDD iteration
   */
  @Input() iteration: number | undefined = undefined;

  /**
   * Currently selected vertex
   */
  @Input() vertexId: number | undefined = undefined;

  ////////////////////////////////////////////////////////////
  // SVG
  ////////////////////////////////////////////////////////////
  //////////////////////////////
  // General
  //////////////////////////////

  @ViewChild('chartContainer') 
  private chartContainerElement!: ElementRef;

  private gvis_inst: Graphviz<d3.BaseType, any, d3.BaseType, any> | undefined;

  constructor(private hfddService: HFDDSessionService) { }

  ngAfterViewInit(): void {
    this.gvis_inst = graphviz(this.chartContainerElement?.nativeElement);
    this.gvis_inst.width(this.chartContainerElement?.nativeElement.offsetWidth);
    this.gvis_inst.height(this.chartContainerElement?.nativeElement.offsetHeight);
    this.gvis_inst = this.gvis_inst
      .transition(() => "linear");
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.hfddSession && this.iteration !== undefined && this.vertexId !== undefined) {
      //const graph_data$ = this.hfddService.getIntraItemsetSankey(this.hfddSession.sessId, this.iteration, this.vertexId)
      //  .subscribe(res => {
      //    console.log(res);
      //  })
      const pdfg_res = this.getPDFG(this.hfddSession, this.vertexId, this.iteration);
      pdfg_res.subscribe(
        {
          next: (pdfgResult:PDFGQueryResult) => {
            this.updateChart(pdfgResult);
          },
          error: (e) => console.log(e)
        });
    }
  }

  getPDFG(hfddSession: HFDDSession, vertexId: number, iteration: number): Observable<PDFGQueryResult> { 
    const ddg_dot$ = this.hfddService.getDiffDDG(hfddSession.sessId, iteration, vertexId);

    return ddg_dot$;

  }

  updateChart(pdfgResult: PDFGQueryResult) {
    if (this.gvis_inst) {
      console.log(pdfgResult);
      this.gvis_inst
        .attributer(function(d) {
          if (d.tag == 'polygon') {
            let v = pdfgResult.diffDFG.vertices[(d.parent.attributes['id'])];
            if (v !== undefined) {
              const probDiff = v.probLeft - v.probRight;
              const col = d3.interpolateRdYlBu(0.5 * probDiff + 0.5);
              //d3.select(this).attr("fill", col);
              d.attributes.fill = col;
            }
          }
          else if (d.tag == 'path') {
            let e = pdfgResult.diffDFG.edges[(d.parent.attributes['id'])];
            if (e !== undefined) {
              const probDiff = e.probLeft - e.probRight;
              const col = d3.interpolateRdYlBu(0.5 * probDiff + 0.5);
              //d3.select(this).attr("fill", col);
              d.attributes.stroke = col;
            }
          }
        })
        .renderDot(pdfgResult.dotString);
    }
  }

}
