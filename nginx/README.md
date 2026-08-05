# Nginx reverse proxy

Configuración simple para enrutar:
- /api/v1/preferences -> client-preferences-service en puerto 8082
- /api/v1/* -> restaurant-service en puerto 8080

## Ejecutar

Desde la carpeta nginx:

```bash
nginx -c nginx.conf
```

Si ya tienes nginx instalado y en ejecución, puedes probar con:

```bash
nginx -s reload
```

## Probar

- http://localhost/api/v1/restaurants
- http://localhost/api/v1/preferences/usuario123
