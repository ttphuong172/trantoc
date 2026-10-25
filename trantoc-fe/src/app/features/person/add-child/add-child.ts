import { Component, OnInit, inject, signal } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { PersonService } from '../../../core/services/person.service';
import { PersonRelationService } from '../../../core/services/person-relation.service';

import { PersonSummary } from '../../../core/models/person-summary.model';
import { PersonDetailResponse } from '../../../core/models/person-detail-response';

@Component({
  selector: 'app-add-child',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-child.html',
  styleUrl: './add-child.css',
})
export class AddChild implements OnInit {
  private readonly personService = inject(PersonService);

  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly relationService = inject(PersonRelationService);

  fullName = '';

  aliasName = '';

  birthYear?: number;

  gender = 'MALE';

  notes = '';

  person = signal<PersonDetailResponse | null>(null);

  spouses = signal<PersonSummary[]>([]);

  selectedMotherId?: number;

  ngOnInit(): void {
    const parentId = Number(this.route.snapshot.paramMap.get('id'));

    this.personService.getPerson(parentId).subscribe((person) => {
      this.person.set(person);
    });

    this.personService.getSpouses(parentId).subscribe((spouses) => {
      this.spouses.set(spouses);

      if (spouses.length === 1) {
        this.selectedMotherId = spouses[0].id;
      }
    });
  }

  save(): void {
    const fatherId = Number(this.route.snapshot.paramMap.get('id'));

    this.personService
      .createPerson({
        fullName: this.fullName,
        aliasName: this.aliasName,
        birthYear: this.birthYear,
        gender: this.gender,
        rootAncestor: false,
        notes: this.notes,
      })
      .subscribe((person) => {
        this.relationService
          .create({
            personId: person.id,
            relatedPersonId: fatherId,
            relationType: 'FATHER',
          })
          .subscribe(() => {
            // Có chọn mẹ
            if (this.selectedMotherId) {
              this.relationService
                .create({
                  personId: person.id,
                  relatedPersonId: this.selectedMotherId,
                  relationType: 'MOTHER',
                })
                .subscribe(() => {
                  this.router.navigate(['/persons', fatherId], {
                    queryParams: this.route.snapshot.queryParams,
                  });
                });

              return;
            }

            // Không chọn mẹ (_)
            this.router.navigate(['/persons', fatherId], {
              queryParams: this.route.snapshot.queryParams,
            });
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
