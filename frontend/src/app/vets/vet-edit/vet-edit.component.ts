/*
 *
 *  * Copyright 2016-2017 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

import {Component, OnInit} from '@angular/core';
import {Vet} from "../vet";
import {VetService} from "../vet.service";
import {SpecialtyService} from "../../specialties/specialty.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Specialty} from "../../specialties/specialty";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-vet-edit',
  templateUrl: './vet-edit.component.html',
  styleUrls: ['./vet-edit.component.css']
})
export class VetEditComponent implements OnInit {
  vet: Vet;
  errorMessage: string;
  allSpecialties: Observable<Specialty[]>;
  selectedSpecialties: String[];


  constructor(private vetService: VetService, private specialtyService: SpecialtyService, private route: ActivatedRoute, private router: Router) {
    this.vet = <Vet>{};
    this.vet.specialties = [];
    this.selectedSpecialties = [];
    this.allSpecialties = specialtyService.getSpecialties();
  }

  ngOnInit() {
    const vetId = this.route.snapshot.params['id'];
    this.vetService.getVetById(vetId).subscribe(
      vet => {
        this.vet = vet;
        this.selectedSpecialties = vet.specialties.map(x => x.id.toString())
      },
      error => this.errorMessage = <any> error
    )
  }

  updateSelectedSpecialty(event: Event) {
    let input = <HTMLInputElement>event.target;
    let value = input.value.valueOf();
    if (input.checked) {
      if (this.selectedSpecialties.indexOf(value) < 0) {
        this.selectedSpecialties.push(value)
      }
    } else {
      if (this.selectedSpecialties.indexOf(value) >= 0) {
        this.selectedSpecialties = this.selectedSpecialties.filter((val, idx, obj) => val != value)
      }
    }
  }

  specialtyChecked(specialty_id: string) {
    return this.vet.specialties.findIndex((v, i, a) => v.id.toString() == specialty_id) >= 0
  }

  onSubmit(vet: Vet) {
    vet.specialties = this.selectedSpecialties.map((val, idx, obj) => {
      let spec = <Specialty>{};
      spec.id = Number(val.valueOf().toString());
      return spec;
    });
    this.vetService.updateVet(vet.id, vet).subscribe(
      resp => {
        this.gotoVetsList();
      },
      error => this.errorMessage = <any>error
    );
  }

  gotoVetsList() {
    const r = this.router;
    setTimeout(function () {
      r.navigate(['/vets']);
    }, 1000)
  }


}
