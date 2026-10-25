import { Component } from '@angular/core';
import { PersonList } from './features/person/person-list/person-list';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [PersonList, RouterOutlet],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  title = 'homes';
}
