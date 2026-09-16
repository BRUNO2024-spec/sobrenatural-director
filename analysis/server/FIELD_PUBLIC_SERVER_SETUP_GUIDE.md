# Guia do servidor Director Research (Forge 1.7.10)

> Estado atual: o servidor mínimo foi criado e testado localmente, mas a
> coleta Shadow V4 permanece desligada até concluir o loader Java e a ligação
> consentimento→coleta. Não anuncie o endereço como servidor de pesquisa ainda.

## PARTE 1 — O que vamos instalar

Uma instalação isolada de Forge 1.7.10 com o mod server-only Sobrenatural
Director. O mundo não é o baseline protegido.

## PARTE 2 — Arquivos necessários

Servidor: `/home/desktop/Documentos/director-shadow-server/`.
Forge e Minecraft já estão disponíveis na VPS; o JAR do Director foi gerado
localmente. O modelo V4 externo está em `models/learned-scorer-v4-model.json`.

## PARTE 3 — Quais mods baixar

Não há mod de gameplay adicional selecionado. Forge 1.7.10-10.13.4.1614 é
necessário no cliente e servidor; origem confiável para um novo download não
foi validada: `NEEDS_USER_PROVIDED_SOURCE`. FNaF/Obsidian não devem ser baixados
para esta instalação.

## PARTE 4 — Onde cada mod deve ficar

No servidor, `SobrenaturalDirector-0.12.0-alpha.jar` fica em
`/home/desktop/Documentos/director-shadow-server/mods/`. Forge fica na raiz.
No cliente, instale apenas o perfil Forge 1.7.10. Não copie o JAR do Director.

## PARTE 5 — Preparar servidor na VPS

```sh
cd /home/desktop/Documentos/director-shadow-server
cat server.properties
```

O template usa `online-mode=true`, whitelist, RCON/query desligados e porta
`25565`.

## PARTE 6 — Abrir porta no Ubuntu

Foi verificado que `ufw status` exige privilégios de root e não foi alterado.
O administrador pode executar, somente se o UFW estiver ativo:

```sh
sudo ufw allow 25565/tcp comment 'Minecraft Director Shadow Research'
sudo ufw status
```

Não altere SSH nem abra intervalos.

## PARTE 7 — Abrir porta na Oracle Cloud

Siga `analysis/server/ORACLE_PUBLIC_PORT_SETUP.md`. A regra Oracle ainda não
foi confirmada.

## PARTE 8 — Iniciar servidor

```sh
/home/desktop/Documentos/director-shadow-server/scripts/start-server.sh
/home/desktop/Documentos/director-shadow-server/scripts/status-server.sh
```

## PARTE 9 — Preparar cliente Minecraft

Crie uma instância Minecraft 1.7.10 usando Forge `10.13.4.1614`. Em Pojav, o
diretório depende da instância selecionada; use a pasta `mods/` daquela
instância. Este pack não requer mod adicional no cliente.

## PARTE 10 — Colocar mods no cliente

Não coloque `SobrenaturalDirector-0.12.0-alpha.jar` no cliente nesta versão.
Não coloque FNaF, ObsidianAPI ou addons FNaF.

## PARTE 11 — Entrar pelo IP público

Após Oracle ingress e teste externo, use `144.22.149.197:25565`. Esse IP foi
detectado, mas ainda não foi confirmado externamente; use o IP do painel Oracle
se ele divergir.

## PARTE 12 — Aceitar participação na coleta

Quando a coleta estiver habilitada, o jogador deverá usar:
`/director consent accept`. Sem isso não deve haver registro de pesquisa.
Atualmente o serviço Shadow está desligado, portanto este comando apenas
registra a preferência de sessão.

## PARTE 13 — Verificar Shadow status

Existe `/director consent status` para a preferência de consentimento. Ainda
não existe comando administrativo `shadow status`; não invente esse comando.

## PARTE 14 — Jogar normalmente

Somente depois de o administrador confirmar `SHADOW_MODEL_LOAD=PASS` e
`CONSENT_GATE=PASS`. O Director determinístico continua sendo a autoridade.

## PARTE 15 — Encerrar corretamente

```sh
/home/desktop/Documentos/director-shadow-server/scripts/stop-server.sh
```

Nunca use `kill -9` em operação normal.

## PARTE 16 — Encontrar os logs

Logs técnicos: `/home/desktop/Documentos/director-shadow-server/logs/`.
Pesquisa futura: `shadow-data/`. O mundo está em `world/` e backups em
`backups/`; nenhum desses diretórios entra no Git.

## PARTE 17 — Importar os dados

Somente offline, depois de validar consentimento e privacidade:

```sh
python3 tools/build-real-experience-corpus.py /caminho/para/shadow-data /caminho/para/corpus
```

Não use Kaggle durante gameplay e não treine automaticamente.

## PARTE 18 — Diagnóstico de erros comuns

- `Address already in use`: confira `ss -lntp | grep 25565`; não inicie outra
  instância.
- `Launch`: confirme `libraries/` e use Java 8.
- `AbstractMethodError`: reconstrua com `./gradlew jar reobf` usando Java 8.
- Falha FNaF/Quaternion: esperado nesta stack; remova FNaF/Obsidian e não
  altere os JARs proprietários.
- Cliente não conecta: confirme whitelist, IP Oracle, TCP 25565 e versão Forge.
