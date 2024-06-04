import { Component } from '@angular/core';
import {RouterOutlet} from "@angular/router";
import {NgDocNavbarComponent, NgDocRootComponent, NgDocSidebarComponent} from "@ng-doc/app";

@Component({
  selector: 'app-ng-doc',
  standalone: true,
  imports: [
    RouterOutlet,
    NgDocSidebarComponent,
    NgDocNavbarComponent,
    NgDocRootComponent
  ],
  templateUrl: './ng-doc.component.html',
  styleUrl: './ng-doc.component.scss'
})
export class NgDocComponent {

}
