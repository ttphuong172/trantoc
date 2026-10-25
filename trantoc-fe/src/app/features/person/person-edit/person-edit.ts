import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { PersonService } from '../../../core/services/person.service';

@Component({
  selector: 'app-person-edit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './person-edit.html',
  styleUrl: './person-edit.css',
})
export class PersonEdit implements OnInit {
  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly personService = inject(PersonService);

  private readonly cdr = inject(ChangeDetectorRef);

  fullName = '';

  aliasName = '';

  birthYear?: number;

  deathYear?: number;

  gender = 'MALE';

  rootAncestor = false;

  notes = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.personService.getPerson(id).subscribe((person) => {
      console.log('PERSON=', person);

      this.fullName = person.fullName;
      this.aliasName = person.aliasName ?? '';
      this.birthYear = person.birthYear;
      this.deathYear = person.deathYear;
      this.gender = person.gender;
      this.rootAncestor = person.rootAncestor;
      this.notes = person.notes ?? '';

      this.cdr.detectChanges();
    });
  }

  save(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.personService
      .updatePerson(id, {
        fullName: this.fullName,
        aliasName: this.aliasName,
        birthYear: this.birthYear,
        deathYear: this.deathYear,
        gender: this.gender,
        rootAncestor: this.rootAncestor,
        notes: this.notes,
      })
      .subscribe(() => {
        this.router.navigate(['/persons', id], {
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
