import {Component, Input, OnInit} from '@angular/core';
import {MatProgressSpinner} from "@angular/material/progress-spinner";
import {NgOptimizedImage} from "@angular/common";

@Component({
  selector: 'app-loader',
  standalone: true,
  imports: [
    MatProgressSpinner,
    NgOptimizedImage
  ],
  templateUrl: './loader.component.html',
  styleUrl: './loader.component.scss'
})
export class LoaderComponent implements OnInit {
  @Input() texts: string[] = [];
  rotatingText = '';
  private textIndex = 0;

  ngOnInit() {
    if (this.texts.length > 0) {
      this.updateText();
      setInterval(() => this.updateText(), 2500);
    }
  }

  updateText() {
    this.rotatingText = this.texts[this.textIndex];
    this.textIndex = (this.textIndex + 1) % this.texts.length;
  }
}
