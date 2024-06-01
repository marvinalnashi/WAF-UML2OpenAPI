import { Component } from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './top-bar.component.html',
  styleUrl: './top-bar.component.scss'
})
export class TopBarComponent {
  isDarkMode = false;

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }
}
