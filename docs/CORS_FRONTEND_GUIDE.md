# Guía Rápida CORS para Desarrolladores Frontend

## 🎯 URL Base del Backend
```
http://localhost:8080/api/v1
```

## 🔐 Autenticación

### 1. Login
```javascript
POST /api/v1/auth/login

// Request
{
  "username": "admin",
  "password": "password123"
}

// Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

### 2. Usar el Token en Peticiones
```javascript
headers: {
  'Authorization': 'Bearer ' + token,
  'Content-Type': 'application/json'
}
```

## 📋 Endpoints Principales

| Endpoint | Método | Requiere Auth | Roles |
|----------|--------|---------------|-------|
| `/auth/login` | POST | No | - |
| `/auth/register` | POST | No | - |
| `/pacientes` | GET | Sí | ADMIN, DOCTOR, ENFERMERO |
| `/pacientes` | POST | Sí | ADMIN, DOCTOR |
| `/consultas` | GET | Sí | ADMIN, DOCTOR, ENFERMERO |
| `/evaluaciones` | GET | Sí | ADMIN, DOCTOR |
| `/sentiment/predict` | POST | Sí | ADMIN, DOCTOR, ANALISTA |
| `/usuarios` | GET | Sí | ADMIN |

## 💻 Ejemplos de Código

### React/Next.js

#### Hook personalizado para API calls
```javascript
// hooks/useApi.js
import { useState } from 'react';

export function useApi() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const baseURL = 'http://localhost:8080/api/v1';

  const callApi = async (endpoint, options = {}) => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem('token');
      const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers
      };

      const response = await fetch(`${baseURL}${endpoint}`, {
        ...options,
        headers
      });

      if (!response.ok) {
        throw new Error(`Error: ${response.status}`);
      }

      const data = await response.json();
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { callApi, loading, error };
}
```

#### Componente de Login
```javascript
// components/Login.jsx
import { useState } from 'react';
import { useApi } from '../hooks/useApi';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { callApi, loading, error } = useApi();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const data = await callApi('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
      });
      
      localStorage.setItem('token', data.token);
      localStorage.setItem('username', data.username);
      console.log('Login exitoso:', data);
      // Redirigir al dashboard
    } catch (err) {
      console.error('Error en login:', err);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <input 
        type="text" 
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        placeholder="Usuario"
      />
      <input 
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Contraseña"
      />
      <button type="submit" disabled={loading}>
        {loading ? 'Cargando...' : 'Iniciar Sesión'}
      </button>
      {error && <div className="error">{error}</div>}
    </form>
  );
}
```

#### Obtener datos
```javascript
// components/Pacientes.jsx
import { useState, useEffect } from 'react';
import { useApi } from '../hooks/useApi';

export default function Pacientes() {
  const [pacientes, setPacientes] = useState([]);
  const { callApi, loading } = useApi();

  useEffect(() => {
    const fetchPacientes = async () => {
      try {
        const data = await callApi('/pacientes');
        setPacientes(data);
      } catch (err) {
        console.error('Error:', err);
      }
    };
    
    fetchPacientes();
  }, []);

  if (loading) return <div>Cargando...</div>;

  return (
    <ul>
      {pacientes.map(p => (
        <li key={p.idPaciente}>{p.nombre} {p.apellido}</li>
      ))}
    </ul>
  );
}
```

### Angular

#### Service
```typescript
// services/api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseURL = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.baseURL}/auth/login`, 
      { username, password }
    );
  }

  getPacientes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseURL}/pacientes`, 
      { headers: this.getHeaders() }
    );
  }

  createPaciente(paciente: any): Observable<any> {
    return this.http.post(`${this.baseURL}/pacientes`, 
      paciente, 
      { headers: this.getHeaders() }
    );
  }
}
```

#### Component
```typescript
// components/login.component.ts
import { Component } from '@angular/core';
import { ApiService } from '../services/api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';

  constructor(
    private apiService: ApiService,
    private router: Router
  ) {}

  onSubmit() {
    this.apiService.login(this.username, this.password)
      .subscribe({
        next: (data) => {
          localStorage.setItem('token', data.token);
          localStorage.setItem('username', data.username);
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.error = 'Error en login';
          console.error(err);
        }
      });
  }
}
```

### Vue.js con Axios

#### API Plugin
```javascript
// plugins/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para agregar token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para manejar errores
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

#### Composable
```javascript
// composables/useAuth.js
import { ref } from 'vue';
import api from '../plugins/api';

export function useAuth() {
  const user = ref(null);
  const loading = ref(false);
  const error = ref(null);

  const login = async (username, password) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await api.post('/auth/login', { 
        username, 
        password 
      });
      
      const { token, username: uname } = response.data;
      localStorage.setItem('token', token);
      localStorage.setItem('username', uname);
      user.value = { username: uname };
      
      return true;
    } catch (err) {
      error.value = err.response?.data?.message || 'Error en login';
      return false;
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    user.value = null;
  };

  return { user, loading, error, login, logout };
}
```

#### Component
```vue
<!-- components/Login.vue -->
<template>
  <form @submit.prevent="handleLogin">
    <input v-model="username" placeholder="Usuario" />
    <input v-model="password" type="password" placeholder="Contraseña" />
    <button type="submit" :disabled="loading">
      {{ loading ? 'Cargando...' : 'Iniciar Sesión' }}
    </button>
    <div v-if="error" class="error">{{ error }}</div>
  </form>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '../composables/useAuth';

const username = ref('');
const password = ref('');
const router = useRouter();
const { login, loading, error } = useAuth();

const handleLogin = async () => {
  const success = await login(username.value, password.value);
  if (success) {
    router.push('/dashboard');
  }
};
</script>
```

## 🔍 Análisis de Sentimientos

```javascript
// Predicción simple
POST /api/v1/sentiment/predict
{
  "text": "Me siento muy feliz hoy"
}

// Response
{
  "text": "Me siento muy feliz hoy",
  "sentiment": "POSITIVE",
  "score": 4,
  "confidence": 0.85
}

// Predicción por lotes
POST /api/v1/sentiment/predict/batch
{
  "texts": [
    "Me siento muy feliz",
    "Estoy triste",
    "No me siento ni bien ni mal"
  ]
}

// Response
{
  "predictions": [
    { "text": "Me siento muy feliz", "sentiment": "POSITIVE", "score": 4 },
    { "text": "Estoy triste", "sentiment": "NEGATIVE", "score": 1 },
    { "text": "No me siento ni bien ni mal", "sentiment": "NEUTRAL", "score": 2 }
  ],
  "aggregateAnalysis": {
    "totalSentences": 3,
    "overallSentiment": "NEUTRAL",
    "averageScore": 2.33,
    "distribution": {
      "POSITIVE": 1,
      "NEGATIVE": 1,
      "NEUTRAL": 1
    }
  }
}
```

## ⚠️ Manejo de Errores

### Códigos de Estado Comunes
- `200`: Éxito
- `201`: Recurso creado
- `400`: Petición inválida
- `401`: No autenticado
- `403`: No autorizado (sin permisos)
- `404`: No encontrado
- `500`: Error del servidor

### Ejemplo de manejo
```javascript
try {
  const response = await fetch('http://localhost:8080/api/v1/pacientes', {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (!response.ok) {
    if (response.status === 401) {
      // Redirigir a login
      window.location.href = '/login';
    } else if (response.status === 403) {
      alert('No tienes permisos para esta acción');
    } else {
      throw new Error(`Error: ${response.status}`);
    }
  }

  const data = await response.json();
  return data;
} catch (error) {
  console.error('Error en la petición:', error);
  throw error;
}
```

## 🧪 Pruebas con cURL

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# GET con token
curl http://localhost:8080/api/v1/pacientes \
  -H "Authorization: Bearer TU_TOKEN_AQUI"

# POST con token
curl -X POST http://localhost:8080/api/v1/pacientes \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Juan","apellido":"Pérez","email":"juan@email.com"}'
```

## 📚 Recursos Adicionales

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`
- **Documentación completa**: Ver `docs/CORS_CONFIGURATION.md`

---

**Nota**: Asegúrate de que el backend esté corriendo en `http://localhost:8080` antes de hacer peticiones.

