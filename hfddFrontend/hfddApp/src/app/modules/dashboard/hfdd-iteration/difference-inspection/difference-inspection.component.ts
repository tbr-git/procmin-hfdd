import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HFDDSession } from '../../../hfdd/hfddsession';
import { HFDDSessionService } from '../../hfddsession.service';
import { DiffCandidateInfo } from 'src/app/modules/hfdd/data/diff-candidate-info';

@Component({
  selector: 'app-difference-inspection',
  templateUrl: './difference-inspection.component.html',
  styleUrls: ['./difference-inspection.component.sass']
})
export class DifferenceInspectionComponent implements OnInit {

  @Input()
  hfddSess: HFDDSession | undefined = undefined;

  @Input()
  iteration: number | undefined = undefined;

  selectedVertexId ?: number = undefined;

  constructor(private hfddSessService: HFDDSessionService) { }

  ngOnInit(): void {
  }

  itemsetSelected(vertex: DiffCandidateInfo) : void {
    this.selectedVertexId = (vertex.idCondActUnion > -1 ? vertex.idCondActUnion : vertex.id);
  }


}
