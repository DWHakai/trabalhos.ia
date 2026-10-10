# Epic Fight → Fabric (port não oficial, uso pessoal)

Port do **Epic Fight 21.17.3.1 (Minecraft 1.21.1, NeoForge)** para **Fabric**.
Base: branch `1.21.1` do repositório oficial, commit `a78aa24`
(`upstream_commit` em `gradle.properties`).

> **Status: `./gradlew build` passa e os eventos comuns de gameplay estão ligados
> (etapa 2).** Ainda não testado em jogo: os alvos dos mixins só são conferidos em runtime.

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
| `PartEntity` (Ender Dragon) | mixins no dragão | **fase 3** |
| Enum extension (`Rarity.EPICFIGHT_UNIQUE`) | não usado pelo mod: `UNIQUE` aponta para `Rarity.EPIC` | feito |
| Métodos que o NeoForge adiciona em classes vanilla (`IItemExtension`, `ClientHooks`, `EventHooks`…) | interfaces injetadas pelo Loom (`fabric/ext`) + mixins, ou o comportamento padrão do NeoForge escrito direto | feito para o que compila |
| Mixins do upstream | revisar alvos que só existem no código patcheado do NeoForge | **fase 3** |
| Integrações (GeckoLib, Iris/Sodium, Controlify, JEI, Curios, KubeJS…) | versões Fabric dessas libs | **fase 4**, opcional |

## Fases

1. **Estrutura** *(atual)*: build Loom, `fabric.mod.json`, entrypoints, plataforma,
   registros, configs, recursos, access widener.
2. **Núcleo de jogo**: eventos de gameplay, rede, attachments, capability de item,
   engines de cliente. Meta: o mundo abre e o modo Epic Fight funciona.
3. **Acabamento**: o que o compilador apontar, `PartEntity` do dragão, enum, revisão
   dos 38 mixins, testes em jogo.
4. **Integrações opcionais** com outros mods.

## Código fora da compilação por enquanto

`build.gradle` exclui, sem apagar, o código que ainda não foi portado:
`client/platform/neoforge/**` (handlers do NeoForge, já refeitos em `fabric/client`), as
integrações ainda não portadas ou sem versão Fabric (`compat/<mod>/**`) e o
datagen (`data/DataEvents`, `data/tags`, `data/recipes`). Os recursos das integrações
estão em `upstream-meta/compat-resources`.

## Arquivos que ainda usam API do NeoForge (fora os excluídos)

Só os que usam `ModConfigSpec`/`ModConfig`, que funcionam via Forge Config API Port.
