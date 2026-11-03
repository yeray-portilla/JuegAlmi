import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonIcon,
  IonSegment, IonSegmentButton, IonLabel, IonTextarea,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { cameraOutline } from 'ionicons/icons';
import { Camera, CameraResultType } from '@capacitor/camera';

@Component({
  standalone: true,
  selector: 'app-soporte',
  templateUrl: './soporte.page.html',
  imports: [
    FormsModule, IonHeader, IonToolbar, IonButtons, IonMenuButton, IonContent, IonButton, IonIcon,
    IonSegment, IonSegmentButton, IonLabel, IonTextarea,
  ],
})
export class SoportePage {
  depto = 'Consolas';
  descripcion = '';
  imagenBase64 = '';

  constructor() {
    addIcons({ cameraOutline });
  }

  // TODO: el precio base vendrá de GET /api/departamentos
  get precioBase() {
    return this.depto === 'Consolas' ? 60 : 40;
  }

  async hacerFoto() {
    const foto = await Camera.getPhoto({ quality: 65, resultType: CameraResultType.Base64 });
    this.imagenBase64 = foto.base64String ?? '';
  }

  enviar() {
    // TODO: POST /api/tickets con { departamento, descripcion, imagen_base64, latitud, longitud }
    console.log(this.depto, this.descripcion, this.imagenBase64.length);
  }
}
