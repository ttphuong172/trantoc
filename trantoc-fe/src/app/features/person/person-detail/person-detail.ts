import { Component, OnInit, inject, signal, HostListener } from '@angular/core';

import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

import { Person } from '../../../core/models/person.model';
import { PersonService } from '../../../core/services/person.service';
import { ParentResponse } from '../../../core/models/parent-response.model';
import { ChildResponse } from '../../../core/models/child-response.model';
import { PersonSummary } from '../../../core/models/person-summary.model';
import { GenerationResponse } from '../../../core/models/generation-response.model';
import { AncestorNode } from '../../../core/models/ancestor-node.model';
import { DescendantNode } from '../../../core/models/descendant-node.model';
import { PersonDetailResponse } from '../../../core/models/person-detail-response';
import { PersonRelationService } from '../../../core/services/person-relation.service';

@Component({
  selector: 'app-person-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './person-detail.html',
  styleUrl: './person-detail.css',
})
export class PersonDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly personService = inject(PersonService);
  private readonly relationService = inject(PersonRelationService);
  private readonly router = inject(Router);

  person = signal<PersonDetailResponse | null>(null);

  parents = signal<ParentResponse | null>(null);

  children = signal<ChildResponse[]>([]);

  spouses = signal<PersonSummary[]>([]);

  ancestorTree = signal<AncestorNode[]>([]);

  descendantTree = signal<DescendantNode | null>(null);

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = Number(params.get('id'));

      this.loadPerson(id);
    });
  }

  private loadPerson(id: number): void {
    this.personService.getPerson(id).subscribe((person) => this.person.set(person));

    this.personService.getParents(id).subscribe((parents) => this.parents.set(parents));

    this.personService.getChildren(id).subscribe((children) => this.children.set(children));

    this.personService.getSpouses(id).subscribe((spouses) => this.spouses.set(spouses));

    this.personService.getAncestorTree(id).subscribe((tree) => {
      this.ancestorTree.set(this.buildAncestorTree(tree));
    });

    this.personService.getDescendantTree(id).subscribe((tree) => {
      this.descendantTree.set(tree);
    });
  }

  private buildAncestorTree(node: any): any[] {
    const result: any[] = [];

    let current = node;

    while (current) {
      result.push(current);

      current = current.parents?.find((p: any) => p.gender === 'MALE');
    }

    return result.reverse();
  }

  private loadChildren(personId: number): void {
    this.personService.getChildren(personId).subscribe((children) => this.children.set(children));
  }

  moveUp(child: ChildResponse): void {
    const fatherId = this.person()?.id;

    if (!fatherId) {
      return;
    }

    this.relationService.moveUp(fatherId, child.id).subscribe(() => {
      this.loadChildren(fatherId);
    });
  }

  moveDown(child: ChildResponse): void {
    const fatherId = this.person()?.id;

    if (!fatherId) {
      return;
    }

    this.relationService.moveDown(fatherId, child.id).subscribe(() => {
      this.loadChildren(fatherId);
    });
  }

  deleteChild(child: ChildResponse): void {
    if (!confirm(`Xóa ${child.fullName}?`)) {
      return;
    }

    this.personService.deletePerson(child.id).subscribe(() => {
      const personId = this.person()?.id;

      if (personId) {
        this.loadChildren(personId);
      }
    });
  }

  changeMother(): void {
    this.router.navigate(['/persons', this.person()?.id, 'change-mother']);
  }

  goToList(): void {
    const queryParams = this.route.snapshot.queryParams;

    this.router.navigate(['/'], {
      queryParams: {
        keyword: queryParams['returnKeyword'] || null,

        generation: queryParams['returnGeneration'] || null,

        page: queryParams['returnPage'] || 0,
      },
    });
  }

  @HostListener('document:keydown', ['$event'])
  handleShortcut(event: KeyboardEvent): void {
    const target = event.target as HTMLElement;

    if (
      target.tagName === 'INPUT' ||
      target.tagName === 'TEXTAREA' ||
      target.tagName === 'SELECT'
    ) {
      return;
    }

    if (event.altKey && event.key.toLowerCase() === 'c') {
      event.preventDefault();

      this.router.navigate(['/persons', this.person()?.id, 'add-child'], {
        queryParamsHandling: 'merge',
      });
    }
  }
}
