# Epic Fight → Fabric (port não oficial, uso pessoal)

Port do **Epic Fight 21.17.3.1 (Minecraft 1.21.1, NeoForge)** para **Fabric**.
Base: branch `1.21.1` do repositório oficial, commit `a78aa24`
(`upstream_commit` em `gradle.properties`).

> **Status: `./gradlew build` passa, com eventos, rede, engines de cliente, tela de config e
> integrações portados.** Ainda não testado em jogo (`./gradlew runClient`).

## Licença

- **Código:** GPL-3.0 (`LICENSE`). Este port continua GPL-3.0.
- **Assets** (texturas, modelos, sons, animações): *All Rights Reserved* pelos autores
  do Epic Fight (`LICENSE-ASSETS`). Por isso **não estão neste repositório**. A task
  `syncUpstreamResources` baixa esses arquivos do repositório oficial, no commit fixado,
  na hora do build. Não redistribua o `.jar` gerado.

## Como compilar (no seu PC)

Requisitos: JDK 21 e git no PATH.

```
cd epicfight-fabric
./gradlew build          # Windows: gradlew.bat build
```

O `.jar` sai em `build/libs/`. Ele precisa de **Fabric API** e **Forge Config API Port**
(o Forge Config API Port já vai embutido no jar).

### Access widener

`src/main/resources/epicfight.accesswidener` junta o access transformer do Epic Fight e o
do próprio NeoForge (o código do upstream conta com os dois), validado contra o jar do
Minecraft (`validateAccessWidener` roda no `build`).

## O que muda do NeoForge para o Fabric

| NeoForge | Fabric (neste port) | Estado |
|---|---|---|
| `@Mod` + mod event bus | `yesman.epicfight.fabric.EpicFightFabric` / `client.EpicFightFabricClient` | escrito |
| `ClientModBusEvent` / `ClientEvents` | `fabric/client/FabricClientEvents` + mixins de login/respawn/logout | feito |
| Config screen (`IConfigScreenFactory`) | `ConfigScreenFactoryRegistry` do Forge Config API Port (Mod Menu) | feito |
| Stencil do `RenderTarget` (editor de datapack) | `MixinRenderTarget` | feito |
| `ModPlatform` (`platform/neoforge`) | `platform/fabric` | escrito |
| `DeferredRegister` / `DeferredHolder` | shim com a mesma API em `platform/registry` | escrito |
| `RegistryBuilder` + `NewRegistryEvent` + callbacks | `platform/registry/RegistryBuilder` sobre `FabricRegistryBuilder` | escrito |
| `ModConfigSpec` / `ModConfigEvent` | Forge Config API Port (mesma API) | escrito |
| `@OnlyIn(Dist.CLIENT)` | `@Environment(EnvType.CLIENT)` | escrito |
| `AddReloadListenerEvent` | `ResourceManagerHelper` + `FabricReloadListener` | escrito |
| `RegisterCommandsEvent` | `CommandRegistrationCallback` | escrito |
| `AddPackFindersEvent` | `registerBuiltinResourcePack` (`resourcepacks/epicfight_legacy`) | escrito |
| `BuildCreativeModeTabContentsEvent` | `ItemGroupEvents.MODIFY_ENTRIES_ALL` | escrito |
| `DataPackRegistryEvent` (emotes) | `DynamicRegistries.registerSynced` | escrito |
| Access transformer | Access widener (AT do Epic Fight + AT do NeoForge) | feito |
| Eventos de gameplay do upstream (`NeoForgeEntityEvent`, `NeoForgePlayerEvent`, `NeoForgeWorldEvent`) | `fabric/FabricCommonEvents` (dano recebido, equipamento, tracking, clone, uso de item, sync de datapack) + mixins em `fabric/mixin/event` nos mesmos pontos dos patches do NeoForge | feito |
| Eventos de cliente (render de entidades, câmera, GUI, input, mão, nameplate) | `platform/client/event` (mesma API) disparados por mixins em `fabric/mixin/client/event` | feito |
| `IEventBasedEngine` (ClientEngine/RenderEngine/ControlEngine) | `IEventBasedEngine.init()` registra nas listas de `platform/client/event` | feito |
| Data Attachments (`NeoForgeRegistries.ATTACHMENT_TYPES`) | campo no `Entity` (`MixinEntityPatchHolder`), criado sob demanda | feito |
| Item capability (`RegisterCapabilitiesEvent`) | `EpicFightCapabilities.getItemCapability` pergunta direto ao provider | feito |
| `AnimationRegistryEvent` (`ModLoader.postEvent`) | evento Fabric `AnimationRegistryEvent.EVENT`, disparado no init | feito |
| Networking (`PayloadRegistrar`, `PacketDistributor`) | `PayloadTypeRegistry` + `ServerPlayNetworking`/`ClientPlayNetworking` | feito |
| Global loot modifiers / `LootTableLoadEvent` | `LootTableEvents.MODIFY`; o modifier do livro de skill via `MixinLootTableModifiers` | feito |
| `PartEntity` (Ender Dragon) | `EnderDragonPart` do vanilla (o único `PartEntity` do jogo; `Level#getEntities` já inclui as partes) | feito |
| Enum extension (`Rarity.EPICFIGHT_UNIQUE`) | não usado pelo mod: `UNIQUE` aponta para `Rarity.EPIC` | feito |
| Métodos que o NeoForge adiciona em classes vanilla (`IItemExtension`, `ClientHooks`, `EventHooks`…) | interfaces injetadas pelo Loom (`fabric/ext`) + mixins, ou o comportamento padrão do NeoForge escrito direto | feito para o que compila |
| Mixins do upstream (38) | conferidos contra o bytecode do Minecraft com `tools/check_mixins.py`; o único que dependia da tabela de locals inteira (`MixinLevelRenderer`) usa `@Local` | feito |
| Integrações com outros mods | as que existem no Fabric, ver "Integrações" abaixo | feito (só compila; não testado com os mods instalados) |
| Loader de modelo `neoforge:separate_transforms` (modelo das armas: 3D na mão, ícone chapado no inventário) | `fabric/client/model/SeparateTransformsModel` (resolver do Fabric Model Loading API) + `MixinItemRendererSeparateTransforms` | feito |
| `Registry#safeCastToReference` aceitando `DeferredHolder` (patch do NeoForge) | `fabric/mixin/MixinRegistry` | feito |

## Fases

1. **Estrutura** *(atual)*: build Loom, `fabric.mod.json`, entrypoints, plataforma,
   registros, configs, recursos, access widener.
2. **Núcleo de jogo**: eventos de gameplay, rede, attachments, capability de item,
   engines de cliente. Meta: o mundo abre e o modo Epic Fight funciona.
3. **Acabamento**: o que o compilador apontar, `PartEntity` do dragão, enum, revisão
   dos 38 mixins, testes em jogo.
4. **Integrações opcionais** com outros mods.

## Integrações

Só as dos mods que têm versão Fabric. Entram como `modCompileOnly` (o jar não depende delas) e
só carregam quando o mod está instalado.

| Mod | Como liga |
|---|---|
| GeckoLib, Iris (+ Sodium), First Person Model, 3D Skin Layers, player-animation-lib | `compat/MinecraftMod` → `ICompatModule`: `onCommonInit` no entrypoint principal, `onClientInit` no de cliente (onde o upstream usava o `FMLClientSetupEvent`) |
| Controlify | entrypoint `controlify` no `fabric.mod.json` (no upstream era `META-INF/services`) |
| Shoulder Surfing | `shouldersurfing_plugin.json` na raiz do mod, igual ao upstream |

Diferenças do upstream:
- GeckoLib: no Fabric o `GeoRenderEvent.Entity.Pre` cancela devolvendo `false`.
- 3D Skin Layers: as malhas ficavam num data attachment do jogador; aqui é um `WeakHashMap` no
  cliente. A skin vem de `player.getSkin().texture()` em vez do `PlayerUtil` do TRansition
  (que no Fabric vem dentro do jar do 3D Skin Layers e não aparece na compilação).
- Sodium: `getQuads` sem `ModelData` e a luz/normal da quad com as cópias de `Mesh.DrawingFunction`.

Descartadas (sem versão Fabric ou fora do escopo): Vampirism, Werewolves, KubeJS, Curios, JEI,
AzureLib, Better Third Person, Wildfire's Female Gender Mod, MCreator, PlayerRevive, Simply
Tooltips. Ficam excluídas no `build.gradle`; os recursos delas estão em
`upstream-meta/compat-resources`.

### Teste automático em jogo

Com a variável `EPICFIGHT_SMOKE_TEST`, o cliente de desenvolvimento entra no mundo, pega as
armas, liga o modo Epic Fight, ataca um husk com cada arma e salva fotos em
`run/screenshots/smoke_*.png`; depois fecha sozinho (`fabric/client/dev/PortSmokeTest`, não faz
nada num jogo normal). Precisa de um mundo chamado "New World" em `run/saves`:

```
EPICFIGHT_SMOKE_TEST=1 ./gradlew runClient --args='--quickPlaySingleplayer "New World"'
```

A mesma classe roda no upstream (NeoForge) para comparar as fotos: registrada com
`@EventBusSubscriber` no `ClientTickEvent.Post`, `devLogin = false` e
`programArguments.addAll("--quickPlaySingleplayer", "New World")` na run `client` (o
`--args` não chega ao jogo no ModDevGradle). No commit a78aa24 o upstream não compila sem
trocar `WeaponCategories.BOW` por `RANGED` (3 arquivos). Comparado em 11/10/2026: as fotos e a
vida dos husks depois de cada combo batem com o upstream, inclusive a primeira pessoa
animada do modo Epic Fight (sem animação de POV, só um pedaço do braço aparece no canto e a
arma fica fora da tela; é assim no upstream também).

### Conferir os mixins sem abrir o jogo

O Mixin só confere os alvos quando a classe carrega. `tools/check_mixins.py` confere todos os
injectors contra o jar do Minecraft mapeado pelo Loom (método alvo, dono, descritor e ordinal
de cada `INVOKE`/`FIELD`):

```
python tools/check_mixins.py <JDK>/bin/javap .gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/<versão>/minecraft-merged-*.jar src/main/java/yesman/epicfight/mixin src/main/java/yesman/epicfight/fabric/mixin
```

## Código fora da compilação por enquanto

`build.gradle` exclui, sem apagar, o código que ainda não foi portado:
`client/platform/neoforge/**` (handlers do NeoForge, já refeitos em `fabric/client`), as
integrações sem versão Fabric (`compat/<mod>/**`) e o
datagen (`data/DataEvents`, `data/tags`, `data/recipes`). Os recursos das integrações
estão em `upstream-meta/compat-resources`.

## Arquivos que ainda usam API do NeoForge (fora os excluídos)

Só os que usam `ModConfigSpec`/`ModConfig`, que funcionam via Forge Config API Port.
