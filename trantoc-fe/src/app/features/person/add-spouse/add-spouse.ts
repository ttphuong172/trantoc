import { Component, OnInit, inject, signal } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ActivatedRoute, Router } from '@angular/router';

import { PersonService } from '../../../core/services/person.service';
import { PersonRelationService } from '../../../core/services/person-relation.service';

import { PersonDetailResponse } from '../../../core/models/person-detail-response';

@Component({
  selector: 'app-add-spouse',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-spouse.html',
})
export class AddSpouse implements OnInit {
  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly personService = inject(PersonService);

  private readonly relationService = inject(PersonRelationService);

  person = signal<PersonDetailResponse | null>(null);

  fullName = '';

  aliasName = '';

  birthYear?: number;

  notes = '';

  ngOnInit(): void {
    const personId = Number(this.route.snapshot.paramMap.get('id'));

    this.personService.getPerson(personId).subscribe((person) => {
      this.person.set(person);
    });
  }

  save(): void {
    const personId = Number(this.route.snapshot.paramMap.get('id'));

    const spouseGender = this.person()?.role === 'SON' ? 'FEMALE' : 'MALE';

    this.personService
      .createPerson({
        fullName: this.fullName,

        aliasName: this.aliasName,

        birthYear: this.birthYear,

        gender: spouseGender,

        rootAncestor: false,

        notes: this.notes,
      })
      .subscribe((spouse) => {
        this.relationService
          .create({
            personId,

            relatedPersonId: spouse.id,

            relationType: 'SPOUSE',
          })
          .subscribe(() => {
            this.router.navigate(['/persons', personId], {
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
