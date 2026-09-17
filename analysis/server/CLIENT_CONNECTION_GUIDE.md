# Guia de conexão do cliente — Director Research

## Requisitos exatos

- Minecraft Java `1.7.10`.
- Forge `10.13.4.1614`.
- **Mods adicionais obrigatórios: nenhum** (`CLIENT_REQUIRED_MOD_COUNT=0`).
- `SobrenaturalDirector-0.12.0-alpha.jar` é server-only nesta instalação e
  não deve ser copiado para o cliente. O servidor aceita a ausência do mod
  por `acceptableRemoteVersions="*"`.

## Arquivos encontrados na VPS

| Arquivo | Local na VPS | SHA-256 | Destino do cliente |
|---|---|---|---|
| `forge-installer.jar` | `/home/desktop/Documentos/forge-server-1.7.10/forge-installer.jar` | `a5d60757d8b305939233a942d5097fa78816d116ef9082c03066fe669e010c7f` | usar como instalador Forge, não como mod |
| `forge-1.7.10-10.13.4.1614-1.7.10-universal.jar` | `/home/desktop/Documentos/director-shadow-server/forge-1.7.10-10.13.4.1614-1.7.10-universal.jar` | `00d1ca02192c7efb87da95552ebf247021f1e1d642f86d0a03076314def11529` | não copiar; é artefato do servidor |

O cliente ainda precisa obter/instalar um perfil Forge 1.7.10-10.13.4.1614.
Não foi inventada URL de download: `NEEDS_DOWNLOAD=YES` se o usuário não
possuir esse instalador localmente.

## Diretórios

Para launcher Java tradicional: use `.minecraft/mods/`, mas não coloque nenhum
JAR adicional nesta configuração. Para PojavLauncher, use a pasta `mods` da
instância/perfil 1.7.10 selecionada; o caminho Android exato é
`USER_LAUNCHER_SPECIFIC` e não é assumido aqui.

## Conexão

Servidor: `144.22.149.197:25565`.

- Autenticação Mojang exigida pelo servidor: **não** (`online-mode=false`).
- Whitelist exigida: **não** (`white-list=false`).
- Acesso não autenticado permite spoofing de username; username não é uma
  identidade de pesquisa confiável.

Inicie o perfil Forge 1.7.10, selecione Multiplayer → Add Server e informe o
 endereço acima. Não instale FNaF, ObsidianAPI ou outros mods desta stack; eles
 não fazem parte do servidor mínimo validado.

## Consentimento Shadow

Depois de entrar:

```text
/director consent status
/director consent accept
/director consent status
```

Para recusar ou revogar:

```text
/director consent decline
/director consent revoke
```

O consentimento é obrigatório e por sessão. Sem `accept`, não há coleta de
pesquisa.

## Erros

- `Mod rejection`: confirme que o cliente está usando exatamente Forge
  `10.13.4.1614` e remova mods adicionais.
- `mismatch`: confirme Minecraft `1.7.10` e o perfil Forge correto.
- Falha de conexão: confirme o endereço, a porta TCP 25565 e se o servidor
  está em execução.
- Para diagnóstico, envie somente mensagem de erro, versão do Minecraft,
  versão Forge e horário aproximado. Não envie UUID, IP, chat ou dados pessoais.
