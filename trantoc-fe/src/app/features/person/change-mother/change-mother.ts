import { Component, OnInit, inject, signal } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { PersonService } from '../../../core/services/person.service';

import { PersonRelationService } from '../../../core/services/person-relation.service';

import { PersonSummary } from '../../../core/models/person-summary.model';
import { PersonDetailResponse } from '../../../core/models/person-detail-response';

@Component({
  selector: 'app-change-mother',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-mother.html',
  styleUrl: './change-mother.css',
})
export class ChangeMother implements OnInit {
  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly personService = inject(PersonService);

  private readonly relationService = inject(PersonRelationService);

  candidateMothers = signal<PersonSummary[]>([]);

  selectedMotherId?: number;

  person = signal<PersonDetailResponse | null>(null);

  ngOnInit(): void {
    const personId = Number(this.route.snapshot.paramMap.get('id'));

    this.personService.getPerson(personId).subscribe((person) => {
      this.person.set(person);
    });

    this.personService.getCandidateMothers(personId).subscribe((mothers) => {
      this.candidateMothers.set(mothers);

      if (mothers.length === 1) {
        this.selectedMotherId = mothers[0].id;
      }
    });
  }

  save(): void {
    if (!this.selectedMotherId) {
      return;
    }

    const personId = Number(this.route.snapshot.paramMap.get('id'));

    this.relationService.changeMother(personId, this.selectedMotherId).subscribe(() => {
      this.router.navigate(['/persons', personId], {
        queryParams: this.route.snapshot.queryParams,
      });
    });
  }

  cancel(): void {
    const personId = Number(this.route.snapshot.paramMap.get('id'));

    this.router.navigate(['/persons', personId], {
      queryParams: this.route.snapshot.queryParams,
    });
  }
}
