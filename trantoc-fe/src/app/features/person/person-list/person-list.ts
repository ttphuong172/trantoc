import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Person } from '../../../core/models/person.model';
import { PersonService } from '../../../core/services/person.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PersonListResponse } from '../../../core/models/person-list-response';

@Component({
  selector: 'app-person-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './person-list.html',
  styleUrl: './person-list.css',
})
export class PersonList implements OnInit {
  currentPage = signal(0);

  totalPages = signal(0);

  totalElements = signal(0);

  private readonly personService = inject(PersonService);

  generations = signal<number[]>([]);

  persons = signal<PersonListResponse[]>([]);

  selectedGeneration = signal<number | null>(null);

  keyword = signal('');

  private readonly router = inject(Router);

  private readonly route = inject(ActivatedRoute);

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const generation = params.get('generation');

      const keyword = params.get('keyword');

      const page = params.get('page');

      this.selectedGeneration.set(generation ? Number(generation) : null);

      this.keyword.set(keyword ?? '');

      this.currentPage.set(page ? Number(page) : 0);

      const hasFilter = this.keyword().trim().length > 0 || this.selectedGeneration() !== null;

      if (hasFilter) {
        this.loadPersons();
      } else {
        this.persons.set([]);

        this.totalElements.set(0);

        this.totalPages.set(0);

        this.currentPage.set(0);
      }
    });

    this.personService.getGenerations().subscribe((data) => {
      this.generations.set(data);
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) {
      return;
    }

    this.currentPage.set(page);

    this.updateQueryParams();
  }

  private loadPersons(): void {
    this.personService
      .getPersons(this.keyword(), this.selectedGeneration() ?? undefined, this.currentPage())
      .subscribe((page) => {
        this.persons.set(page.content);

        this.currentPage.set(page.number);

        this.totalPages.set(page.totalPages);

        this.totalElements.set(page.totalElements);
      });
  }

  onGenerationChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;

    this.selectedGeneration.set(value ? Number(value) : null);

    this.currentPage.set(0);

    this.updateQueryParams();
  }

  onKeywordChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;

    this.keyword.set(value);

    this.currentPage.set(0);

    this.updateQueryParams();
  }

  private updateQueryParams(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        keyword: this.keyword() || null,
        generation: this.selectedGeneration() ?? null,
        page: this.currentPage(),
      },
    });
  }
}
