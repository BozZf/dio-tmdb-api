# DIO TMDB API

API REST em Java 17 e Spring Boot que consulta filmes no The Movie Database (TMDB). A integração usa Spring Cloud OpenFeign.

## Executar localmente

Tenha o JDK 17 ou superior e uma chave de API v3 do TMDB. O Maven Wrapper baixa o Maven na primeira execução. [Como obter uma chave do TMDB](https://developer.themoviedb.org/docs/authentication-application).

```bash
git clone https://github.com/BozZf/dio-tmdb-api.git
cd dio-tmdb-api
read -rsp 'Chave da API do TMDB: ' TMDB_API_KEY
echo
export TMDB_API_KEY
./mvnw spring-boot:run
```

No PowerShell 7.2 ou superior, use `$env:TMDB_API_KEY = Read-Host "Chave da API do TMDB" -MaskInput` e execute `.\mvnw.cmd spring-boot:run`. A variável vale para a sessão do terminal. Mantenha a chave fora dos arquivos versionados.

## Endpoints

A aplicação atende em `http://localhost:8080` por padrão.

| Método e rota | Parâmetro obrigatório | Resposta `200` |
| --- | --- | --- |
| `GET /movie/{id}` | `id` inteiro positivo no caminho | Objeto com os dados do filme |
| `GET /search/movie` | `query` com o título ou parte dele | Array de filmes da primeira página do TMDB. Retorna `[]` quando não há resultados. |

```bash
curl 'http://localhost:8080/movie/603'
curl --get --data-urlencode 'query=The Matrix' 'http://localhost:8080/search/movie'
```

As respostas usam campos em camelCase, como `posterPath`, `releaseDate` e `voteAverage`. Campos ausentes no retorno do TMDB podem ser `null`.

| Status | Significado |
| --- | --- |
| `400` | ID inválido ou busca sem texto |
| `404` | O TMDB não encontrou o filme solicitado pelo ID |
| `502` | Falha ao consultar o TMDB ou resposta inesperada |
| `504` | O TMDB excedeu o tempo limite |

Os erros usam `application/problem+json` e incluem campos como `status`, `title` e `detail`.

- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [Especificação OpenAPI em JSON](http://localhost:8080/v3/api-docs)

## Testes

```bash
./mvnw test
```

No Windows, execute `.\mvnw.cmd test`. Os testes exercitam as rotas, o contrato OpenAPI e as chamadas Feign contra um servidor HTTP local, com uma chave fictícia. A execução da suíte dispensa uma chave real e acesso ao TMDB.

## Estrutura

- `adapter/controller` recebe e valida as requisições HTTP.
- `service/tmdb` concentra a consulta e valida o retorno do TMDB.
- `adapter/tmdb` define o cliente Feign.
- `adapter/handler` converte falhas do cliente em respostas HTTP.
- `model` define a representação dos filmes e dos resultados de busca.
