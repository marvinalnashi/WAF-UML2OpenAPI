import {Component, Input, OnInit} from '@angular/core';
import {MatProgressSpinner} from "@angular/material/progress-spinner";
import {NgOptimizedImage} from "@angular/common";

/**
 * Loader component for displaying the loader/spinner overlay and its contents.
 */
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
  /**
   * Input property that sets strings as rotating texts in an array for the loader/spinner overlay.
   */
  @Input() texts: string[] = [];

  /**
   * The string that is currently being displayed in the loader/spinner overlay.
   */
  rotatingText = '';

  /**
   * The index of the string that is currently being displayed in the loader/spinner overlay.
   */
  private textIndex = 0;


  /**
   * Sets the interval in milliseconds between the strings that are rotated in the loader/spinner overlay.
   */
  ngOnInit() {
    if (this.texts.length > 0) {
      this.updateText();
      setInterval(() => this.updateText(), 2500);
    }
  }

  /**
   * Updates the text that is currently being displayed in the loader/spinner overlay.
   */
  updateText() {
    this.rotatingText = this.texts[this.textIndex];
    this.textIndex = (this.textIndex + 1) % this.texts.length;
  }
}
