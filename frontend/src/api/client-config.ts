import { type CreateClientConfig } from './generated/client.gen'

// Le contrat déclare le serveur « / » et des chemins qui commencent par /api : sans cette
// surcharge, le client concatène les deux en //api/…, une URL relative au protocole.
// Les appels partent donc vers la même origine (proxy Vite en dev), avec le cookie de session ;
// le jeton CSRF, posé par le serveur dans le cookie XSRF-TOKEN, revient en en-tête (ADR-015).
export const createClientConfig: CreateClientConfig = (config) => ({
  ...config,
  baseURL: '',
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})
