# Epic Fight → Fabric (port não oficial, uso pessoal)

Port do **Epic Fight 21.17.3.1 (Minecraft 1.21.1, NeoForge)** para **Fabric**.
Base: branch `1.21.1` do repositório oficial, commit `a78aa24`
(`upstream_commit` em `gradle.properties`).

> **Status: fase 1 em andamento. Ainda não compila nem roda.**
> Veja "Bloqueio atual" abaixo.

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

### Primeiro build: access widener

O NeoForge usa *access transformers* e o Fabric usa *access wideners*. O conversor
`tools/at2aw.py` já converteu métodos e classes, mas os campos precisam do descritor de
tipo, que só existe no jar do Minecraft. Depois do primeiro `./gradlew build`, mesmo que
falhe na compilação, rode:

```
python3 tools/at2aw.py upstream-meta/accesstransformer.cfg \
  "$(find ~/.gradle/caches/fabric-loom -name 'minecraft-merged-*.jar' -path '*layered*' | head -1)" \
  > src/main/resources/epicfight.accesswidener
```

## Bloqueio atual

O ambiente de nuvem onde este port está sendo feito bloqueia o acesso de rede a:

- `maven.fabricmc.net` (Fabric Loom, Loader, API)
- `piston-meta.mojang.com`, `piston-data.mojang.com`, `libraries.minecraft.net`,
  `resources.download.minecraft.net`, `launchermeta.mojang.com` (Minecraft)
- `maven.parchmentmc.org` (nomes de parâmetros)

Sem eles, não dá para baixar o Minecraft nem compilar. Até esses domínios serem liberados,
o código está sendo escrito sem compilador, então espere erros de compilação no primeiro
build.

## O que muda do NeoForge para o Fabric

| NeoForge | Fabric (neste port) | Estado |
|---|---|---|
| `@Mod` + mod event bus | `yesman.epicfight.fabric.EpicFightFabric` / `client.EpicFightFabricClient` | escrito |
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
| Access transformer | Access widener (`tools/at2aw.py`) | métodos ok, campos pendentes |
| ~60 eventos de gameplay (dano, knockback, tick, morte, montaria, efeitos…) | callbacks do Fabric API onde existem, mixins próprios no resto | **fase 2** |
| Eventos de cliente (render de entidades, câmera, GUI, input, mão, nameplate) | mixins no `LivingEntityRenderer`, `GameRenderer`, `Gui`, `MouseHandler`… | **fase 2** |
| `IEventBasedEngine` (ClientEngine/RenderEngine/ControlEngine) | ligar às callbacks novas | **fase 2** |
| Data Attachments (`NeoForgeRegistries.ATTACHMENT_TYPES`) | Fabric Data Attachment API | **fase 2** |
| Item capability (`RegisterCapabilitiesEvent`) | lookup próprio (cache por item) | **fase 2** |
| `AnimationRegistryEvent` (`ModLoader.postEvent`) | callback simples chamada no init | **fase 2** |
| Networking (`PayloadRegistrar`, `PacketDistributor`) | `PayloadTypeRegistry` + `ServerPlayNetworking`/`ClientPlayNetworking` | **fase 2** |
| Global loot modifiers / `LootTableLoadEvent` | `LootTableEvents.MODIFY` | **fase 2** |
| `PartEntity` (Ender Dragon) | mixins no dragão | **fase 3** |
| Enum extension (`Rarity.EPICFIGHT_UNIQUE`) | mixin no enum ou usar outra cor | **fase 3** |
| Métodos que o NeoForge adiciona em classes vanilla (`IItemExtension`, `ClientHooks`, `EventHooks`…) | mixins / chamadas vanilla equivalentes | **fase 3**, aparecem no compilador |
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
`client/platform/neoforge/**` (handlers de evento), `compat/*/**` (integrações) e
datagen (`data/DataEvents`, `data/tags`, `data/recipes`). Os recursos das integrações
estão em `upstream-meta/compat-resources`.

## Arquivos que ainda usam API do NeoForge (fora os excluídos)

Arquivos que só usam `ModConfigSpec`/`ModConfig` já funcionam via Forge Config API Port.

- `yesman/epicfight/api/animation/AnimationManager.java`
- `yesman/epicfight/api/animation/types/AttackAnimation.java`
- `yesman/epicfight/api/asset/JsonAssetLoader.java`
- `yesman/epicfight/api/client/camera/EpicFightCameraAPI.java`
- `yesman/epicfight/api/client/event/types/camera/BuildCameraTransform.java`
- `yesman/epicfight/api/client/model/Mesh.java`
- `yesman/epicfight/api/collider/Collider.java`
- `yesman/epicfight/api/collider/MultiCollider.java`
- `yesman/epicfight/api/event/impl/VanillaEntityEventHooks.java`
- `yesman/epicfight/api/event/types/player/ChangeInnateSkillEvent.java`
- `yesman/epicfight/api/utils/EntitySnapshot.java`
- `yesman/epicfight/api/utils/side/ClientOnly.java`
- `yesman/epicfight/client/ClientEngine.java`
- `yesman/epicfight/client/events/engine/ControlEngine.java`
- `yesman/epicfight/client/events/engine/IEventBasedEngine.java`
- `yesman/epicfight/client/events/engine/RenderEngine.java`
- `yesman/epicfight/client/gui/HealthBar.java`
- `yesman/epicfight/client/gui/VersionNotifier.java`
- `yesman/epicfight/client/gui/screen/SkillBookScreen.java`
- `yesman/epicfight/client/gui/screen/config/EpicFightSettingScreen.java`
- `yesman/epicfight/client/input/CombatKeyMapping.java`
- `yesman/epicfight/client/input/EpicFightKeyMappings.java`
- `yesman/epicfight/client/input/InputUtils.java`
- `yesman/epicfight/client/renderer/EpicFightShaders.java`
- `yesman/epicfight/client/renderer/VanillaFakeBlockRenderer.java`
- `yesman/epicfight/client/renderer/blockentity/FractureBlockRenderer.java`
- `yesman/epicfight/client/renderer/entity/NoopLivingEntityRenderer.java`
- `yesman/epicfight/client/renderer/patched/entity/PatchedEntityRenderer.java`
- `yesman/epicfight/client/renderer/patched/layer/WearableItemLayer.java`
- `yesman/epicfight/client/renderer/shader/compute/loader/ComputeShaderProvider.java`
- `yesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch.java`
- `yesman/epicfight/compat/ICompatModule.java`
- `yesman/epicfight/compat/MinecraftMod.java`
- `yesman/epicfight/compat/ModMixinPlugin.java`
- `yesman/epicfight/config/ClientConfig.java`
- `yesman/epicfight/config/CommonConfig.java`
- `yesman/epicfight/config/ServerConfig.java`
- `yesman/epicfight/data/loot/EpicFightLootTables.java`
- `yesman/epicfight/data/loot/OnSkillBookDroppedByEntity.java`
- `yesman/epicfight/epicskins/animation/EpicSkinsAnimations.java`
- `yesman/epicfight/epicskins/user/AuthenticationHelperImpl.java`
- `yesman/epicfight/fabric/EpicFightFabric.java`
- `yesman/epicfight/gameasset/Animations.java`
- `yesman/epicfight/main/AuthenticationHelper.java`
- `yesman/epicfight/main/EpicFightExtensibleEnums.java`

- `yesman/epicfight/mixin/common/MixinWitherBoss.java`
- `yesman/epicfight/network/EpicFightClientBoundPayloadHandler.java`
- `yesman/epicfight/network/EpicFightNetworkManager.java`
- `yesman/epicfight/network/EpicFightServerBoundPayloadHandler.java`
- `yesman/epicfight/registry/entries/EpicFightAttachmentTypes.java`
- `yesman/epicfight/registry/entries/EpicFightAttributes.java`
- `yesman/epicfight/registry/entries/EpicFightEntityTypes.java`
- `yesman/epicfight/registry/entries/EpicFightGlobalLootModifiers.java`
- `yesman/epicfight/world/capabilities/EpicFightCapabilities.java`
- `yesman/epicfight/world/capabilities/GlobalCapability.java`
- `yesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/boss/WitherGhostPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/boss/WitherPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/boss/enderdragon/EnderDragonPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/CreeperPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/EndermanPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/HoglinPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/IronGolemPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/PiglinBrutePatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/PiglinPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/RavagerPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/VindicatorPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/WitherSkeletonPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/ZoglinPatch.java`
- `yesman/epicfight/world/capabilities/entitypatch/mob/ZombiePatch.java`
- `yesman/epicfight/world/capabilities/item/CapabilityItem.java`
- `yesman/epicfight/world/capabilities/provider/AttachmentEntityPatchProvider.java`
- `yesman/epicfight/world/capabilities/provider/CommonItemCapabilityProvider.java`
- `yesman/epicfight/world/damagesource/ExtraDamageInstance.java`
- `yesman/epicfight/world/gamerule/EpicFightGameRules.java`
- `yesman/epicfight/world/item/WeaponItem.java`
