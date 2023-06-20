import { AfterViewInit, Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { VertexMeasurement } from '../../../../hfdd/data/vertex-measurement';
import { HFDDSession } from '../../../../hfdd/hfddsession';
import { HFDDSessionService } from '../../../hfddsession.service';

import * as d3 from 'd3';

@Component({
  selector: 'app-metric-overview',
  templateUrl: './metric-overview.component.html',
  styleUrls: ['./metric-overview.component.sass']
})
export class MetricOverviewComponent implements AfterViewInit, OnChanges {

  @Input() hfddSession: HFDDSession | undefined = undefined;

  @Input() iteration: number | undefined = undefined;

  @ViewChild('chartContainer') 
  private chartContainerElement!: ElementRef;

  private heightValue = 300;

  private widthValue = 600;

  private margin: { top: number, bottom: number, left: number; right: number} =  {top: 20, bottom: 30, left: 30, right: 20};
  
  /**
   * Binning function
   */
  histogram: d3.HistogramGeneratorNumber<VertexMeasurement, number>;

  contentGroup: d3.Selection<SVGGElement, unknown, null, undefined> | undefined;

  g_y_axis: d3.Selection<SVGGElement, unknown, null, undefined> | undefined;

  x: d3.ScaleLinear<number, number, never>;

  constructor(private hfddService: HFDDSessionService) {
    // set the parameters for the histogram
    this.histogram = d3.bin<VertexMeasurement, number>()
        .value((d: VertexMeasurement) => d.metric)   // I need to give the vector of value
        .thresholds(20); // then the numbers of bins

     // X axis: scale and draw:
    var width = this.widthValue - this.margin.left - this.margin.right;
    this.x = d3.scaleLinear()
        .domain([0, 1])     
        .range([0, width]);
   }

  ngAfterViewInit(): void {
    const svgContainer = d3.select(this.chartContainerElement?.nativeElement);

    const svg = svgContainer.append("svg")
      .attr("viewBox", `0 0 ${this.widthValue} ${this.heightValue}`)
      .attr("width", "100%")
      .attr("height", "100%")
    
    var height = this.heightValue - this.margin.bottom - this.margin.top;

    /*
        Then we append a group element inside the svg that will
        then contain chart. We add a translation to respect the margin.
        This margin is important because the text of the x- and y-axis
        will go in the space it reserves.
      */
    this.contentGroup = svg.append('g')
      .attr('transform', `translate(${this.margin.left},${this.margin.top})`)

    this.contentGroup.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(this.x)) 

    var y = d3.scaleLinear()
        .range([height, 0])
        .domain([0, 10]);   

    this.g_y_axis = this.contentGroup.append("g")
      .attr("class", "y axis");

  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.hfddSession && this.iteration !== undefined) {
      this.hfddService.getMetrics(this.hfddSession.sessId, this.iteration).subscribe(
        data => this.createHistogram(data));
    }
  }

  private createHistogram(data: VertexMeasurement[]): void {

    console.log("Creating Histogram")
    var height = this.heightValue - this.margin.bottom - this.margin.top;

    // And apply this function to data to get the bins
    var bins = this.histogram(data);

    if (this.contentGroup && this.g_y_axis) {
      console.log("Setup is there!")
      // Y axis: scale and draw:
      var y = d3.scaleLinear()
          .range([height, 0])
          .domain([0, d3.max(bins, function(d) { return d.length; }) || 1]);   // d3.bins has to be called before the Y axis obviously

      this.g_y_axis
          .call(d3.axisLeft(y));

      if (bins) {
        const selectedBars = this.contentGroup.selectAll("rect").data(bins);
        selectedBars
          .enter()
          .append("rect")
            .attr("x", 1)
            .attr("transform", (d) => { return "translate(" + this.x(d.x0 || 0) + "," + y(d.length) + ")"; }) // Better Defaults!!!
            .attr("width", (d) => { return this.x(d.x1 || 10) - this.x(d.x0 || 0) - 1 ; })
            .attr("height", (d) => { return height - y(d.length); })
            .style("fill", "#69b3a2");
        
        selectedBars.merge(selectedBars)
          .transition()
            .attr("transform", (d) => { return "translate(" + this.x(d.x0 || 0) + "," + y(d.length) + ")"; }) // Better Defaults!!!
            .attr("width", (d) => { return this.x(d.x1 || 10) - this.x(d.x0 || 0) - 1 ; })
            .attr("height", (d) => { return height - y(d.length); });

        selectedBars.exit().remove();
      }
    }
  }

}
