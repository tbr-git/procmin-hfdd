import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-fileupload',
  templateUrl: './fileupload.component.html',
  styleUrls: ['./fileupload.component.sass']
})
export class FileuploadComponent implements OnInit {

  fileName = '';

    constructor(private http: HttpClient) {}

    onFileSelected(event:any) {

        const file:File = event.target.files[0];

        if (file) {

            this.fileName = file.name;

            const formData = new FormData();

            formData.append("thumbnail", file);

            //const upload$ = this.http.post("/api/thumbnail-upload", formData);

            //upload$.subscribe();
        }
    }

  ngOnInit(): void {
  }

}
