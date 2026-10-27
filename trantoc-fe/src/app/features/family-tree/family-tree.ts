import { Component, inject, OnInit, signal } from '@angular/core';
import * as d3 from 'd3';
import { PersonService } from '../../core/services/person.service';

@Component({
  selector: 'app-family-tree',
  standalone: true,
  templateUrl: './family-tree.html',
})
export class FamilyTree implements OnInit {
  private readonly personService = inject(PersonService);

  paternalLine = signal<string[]>([]);

  ngOnInit(): void {
    this.personService.getAncestorTree(13).subscribe((data) => {
      const line = this.buildPaternalLine(data);

      this.paternalLine.set(line);
    });

    this.personService.getDescendantTree(13).subscribe((data) => {
      const familyTree = this.buildFamilyTree(this.paternalLine(), data);

      const root = d3.hierarchy(familyTree);

      const treeLayout = d3.tree<any>().nodeSize([80, 300]);

      treeLayout(root);

      d3.select('#tree').selectAll('*').remove();

      const svg = d3
        .select('#tree')
        .append('svg')
        .attr('width', '100%')
        .attr('height', 5000)
        .style('cursor', 'grab');

      const g = svg.append('g');

      const zoom = d3
        .zoom<SVGSVGElement, unknown>()
        .scaleExtent([0.2, 5])
        .on('zoom', (event) => {
          g.attr('transform', event.transform);
        });

      svg.call(zoom);

      svg.call(zoom.transform, d3.zoomIdentity.translate(500, 50).scale(1));



      // Links
      g.selectAll('line')
        .data(root.links())
        .enter()
        .append('line')
        .attr('x1', (d) => d.source.x!)
        .attr('y1', (d) => d.source.y! + 40) // đáy card cha
        .attr('x2', (d) => d.target.x!)
        .attr('y2', (d) => d.target.y! - 40) // đỉnh card con
        .attr('stroke', '#000');

      // Nodes
      const nodes = g
        .selectAll('.node')
        .data(root.descendants())
        .enter()
        .append('g')
        .attr('class', 'node')
        .attr('transform', (d) => `translate(${d.x},${d.y})`);

      // Card
      nodes
        .append('rect')
        .attr('x', (d: any) => {
          switch (d.depth) {
            case 0:
              return -400;

            default:
              return -30;
          }
        })
        .attr('y', -40)
        .attr('width', (d: any) => {
          switch (d.depth) {
            case 0:
              return 800;

            default:
              return 60;
          }
        })
        .attr('height', 80)
        .attr('rx', 4)
        .attr('fill', '#fff')
        .attr('stroke', '#000')
        .attr('stroke-width', (d: any) => {
          switch (d.depth) {
            case 0:
              return 4;

            default:
              return 2;
          }
        });

      // Full name vertical
      nodes.each(function (d) {

          if (d.depth === 0) {
            d3.select(this)
              .append('text')
              .attr('text-anchor', 'middle')
              .attr('dominant-baseline', 'middle')
              .style('font-size', '48px')
              .style('font-weight', '700')
              .text(d.data.fullName);

            return;
          }

          const lines = d.data.fullName.split(' ');

          const text = d3
            .select(this)
            .append('text')
            .attr('text-anchor', 'middle')
            .style('font-size', '11px');

          lines.forEach((line: string, index: number) => {
            text
              .append('tspan')
              .attr('x', 0)
              .attr('y', -20 + index * 18)
              .text(line);
          });
        });
    });
  }

  private buildPaternalLine(node: any): string[] {
    const result: string[] = [];

    let current = node;

    while (current) {
      result.push(current.fullName);

      current = current.parents?.find((p: any) => p.gender === 'MALE');
    }

    return result.reverse();
  }

  private buildFamilyTree(paternalLine: string[], descendants: any): any {
    const root: any = {
      fullName: paternalLine[0],
      children: [],
    };

    let current = root;

    for (let i = 1; i < paternalLine.length; i++) {
      const node = {
        fullName: paternalLine[i],
        children: [],
      };

      current.children.push(node);

      current = node;
    }

    current.children = descendants.children;

    return root;
  }
}
