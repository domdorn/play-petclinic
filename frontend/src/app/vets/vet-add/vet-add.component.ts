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

/**
 * @author Vitaliy Fedoriv
 */

import {Component, OnInit} from '@angular/core';
import {VetService} from '../vet.service';
import {Vet} from '../vet';
import {Router} from '@angular/router';

@Component({
  selector: 'app-vet-add',
  templateUrl: './vet-add.component.html',
  styleUrls: ['./vet-add.component.css']
})
export class VetAddComponent implements OnInit {

  vet: Vet;
  errorMessage: string;

  constructor(private vetService: VetService, private router: Router) {
    this.vet = <Vet>{};
  }

  ngOnInit() {
  }

  onSubmit(vet: Vet) {
    vet.id = null;
    this.vetService.addVet(vet).subscribe(
      new_vet => {
        this.vet = new_vet;
        this.gotoVetsList();
      },
      error => this.errorMessage = <any>error
    );
  }

  gotoVetsList() {
    this.router.navigate(['/vets']);
  }

}
