import { NgDocRootComponent, NgDocNavbarComponent, NgDocSidebarComponent } from "@ng-doc/app";
import {Component, OnInit} from '@angular/core';
import { GenerationComponent } from './generation/generation.component';
import {NgIf} from "@angular/common";
import {LoaderComponent} from "./loader/loader.component";
import {NgDocComponent} from "./ng-doc/ng-doc.component";
import {TopBarComponent} from "./top-bar/top-bar.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [GenerationComponent, NgIf, LoaderComponent, NgDocRootComponent, NgDocNavbarComponent, NgDocSidebarComponent, NgDocComponent, TopBarComponent],
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  isLoading = true;
  showDocumentation = false;

  ngOnInit() {
    setTimeout(() => {
      this.isLoading = false;
    }, 2000);
  }

  toggleDocumentation() {
    this.showDocumentation = !this.showDocumentation;
  }
}
