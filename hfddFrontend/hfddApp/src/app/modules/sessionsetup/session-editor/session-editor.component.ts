import { HttpEventType } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { HFDDSessionService } from '../../dashboard/hfddsession.service';
import { Router } from '@angular/router';


@Component({
  selector: 'app-session-editor',
  templateUrl: './session-editor.component.html',
  styleUrls: ['./session-editor.component.sass']
})
export class SessionEditorComponent implements OnInit {

  sessionForm = new UntypedFormGroup({
    sessionName: new UntypedFormControl('', [Validators.required, Validators.minLength(1)]),
    sessionDesc: new UntypedFormControl('', [Validators.required]),
    fileLogLeft: new UntypedFormControl('', [Validators.required]),
    fileLogRight: new UntypedFormControl('', [Validators.required]),
    prepLoopUnroll: new FormControl<number>(0, [Validators.required, Validators.min(-1)]),
    spsMiningTime: new FormControl<number>(10000, [Validators.required, Validators.min(100)]),
    spsTargetSize: new FormControl<number>(10000, [Validators.required, Validators.min(1)]),
    spsTargetSizeMargin: new FormControl<number>(0.1, [Validators.required, Validators.min(0.01), Validators.max(0.99)]),
  })


  fileNameLeft = '';
  fileNameRight = '';

  showUploadProgress = false;
  uploadProgressLeft: number = 0;
  uploadProgressRight: number = 0;

  sessionReady = false;

  constructor(private router: Router, private hfddSessService : HFDDSessionService) { }

  ngOnInit(): void {
  }

  /**
   * Left file selected.
   * @param event Input file selection event
   */
  onLeftFileSelected(event:any) {
    if (event.target.files.length > 0) {

      const file = event.target.files[0];
      this.fileNameLeft = file.name;

      this.sessionForm.patchValue({
        fileLogLeft: file
      });
    }
  }

  /**
   * Right file selected.
   * @param event Input file selection event
   */
  onRightFileSelected(event:any) {
    if (event.target.files.length > 0) {

      const file = event.target.files[0];
      this.fileNameRight = file.name;

      this.sessionForm.patchValue({
        fileLogRight: file
      });
    }
  }

  onSessionCreateClick() {

    let [hfddSession$, uploadLeft$, uploadRight$, initComparison$] =
      this.hfddSessService.createSession(
        this.sessionForm.get('sessionName')?.value,
        this.sessionForm.get('sessionDesc')?.value,
        this.sessionForm.get('fileLogLeft')?.value,
        this.sessionForm.get('fileLogRight')?.value,
        this.sessionForm.get('spsMiningTime')?.value,
        this.sessionForm.get('spsTargetSize')?.value,
        this.sessionForm.get('spsTargetSizeMargin')?.value,
        this.sessionForm.get('prepLoopUnroll')?.value
    );

    this.showUploadProgress = true;

    uploadLeft$.subscribe(e => {
        if (e.type == HttpEventType.UploadProgress) {
          this.uploadProgressLeft = Math.round(100 * (e.loaded / e.total));
        }
    });

    uploadRight$.subscribe(e => {
        if (e.type == HttpEventType.UploadProgress) {
          this.uploadProgressRight = Math.round(100 * (e.loaded / e.total));
        }
    });

    forkJoin({hfddSess: hfddSession$, initialization: initComparison$}).subscribe(res => {
      console.log('Initialized');
      //this.router.navigate(['/comparisonSession'], { queryParams: {sessId: res['hfddSess'].sessId}})
      this.router.navigate(['/comparisonSession', res['hfddSess'].sessId]);
    })
  }
}
