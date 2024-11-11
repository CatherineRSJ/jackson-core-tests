# Modification de la Github Action
La modification de la Github Action était plutôt simple. En faisant des recherches, nous avons vu que 2 solutions s'offraient à nous.
La première était de regrouper les 3 actions (Tests, Jacoco et Seuil) dans une seule et de faire une boucle `for` pour itérer à travers les flags.
L'autre solution était d'utiliser la fonctionnalité de `matrix` des Github Action et de garder les 3 actions intactes.
Nous avons opté pour la dernière puisque nous trouvions cette solution plus simple et plus conforme aux normes Github.
Il s'agissait seulement de définir une liste de `jvm_flags` dans une `matrix` et la Github Action s'occupe de répéter les `steps` pour chacun des flags.

```yaml
strategy:
    matrix:
        jvm_flags: ["Flag1", "Flag2"]
```

Ensuite, il suffisait de récupérer le flag courant et de l'envoyer en paramètre à `mvn verify` comme ceci.

```yaml
- name: Run Maven tests with ${{ matrix.jvm_flags }}
  run: mvn verify -DargLine="@{argLine} ${{ matrix.jvm_flags }}"
```

Nous avons recontré quelques problème avec `-DargLine` et JaCoCo.
En effet, nous avons commencé par faire simplement ceci.

`-DargLine="${{ matrix.jvm_flags }}"`

L'action de tests fonctionnait parfaitement, le problème était au niveau de JaCoCo.
Le dossier `target` ne se générait pas de la bonne façon.
Cela était dû au fait que JaCoCo utilise `-DargLine` pour générer le code coverage, et le paramètre `@{argLine}` est ce qu'utilise JaCoCo, donc il est important de toujours l'envoyer.

Nous nous sommes inspirés de [cette vidéo Youtube](https://youtu.be/xvFZjo5PgG0) pour mieux comprendre comment fonctionnaient les tests et les Github Action.

# Justification
## Xmx / Xms (type: Heap)
Nous considerons ces 2 flags comme un seul puisqu'ils sont très semblables.
En effet, les 2 vont assister à la gestion du heap de mémoire.
Réduire la taille maximale du heap permettra de vérifier que les tests s'exécutent bien en simulant des appareils avec peu de ressources disponibles.
Dans un autre temps, réduire la taille initiale du heap permettra de voir si les tests s'exécutent adéquatement même s'ils doivent venir augmenter la taille du heap en raison de manque de mémoire.
L'inverse est aussi un aspect intéressant à tester ; s'assurer que les tests fonctionnent bien avec un heap initial d'une bonne taille qui n'a pas besoin d'augmenter.
Comme Jackson est une librairie de traitement de JSON, et qu'il est fréquent d'avoir des données lourdes à traiter, il est cruciale de tester les accès à la mémoire de l'appareil.

## UseParallelGC (type: GC)
Avec G1 étant le GC (garbage collector) par défaut, nous trouvions nécessaire de valider les tests avec un GC différent.
Le GC parallèle était celui par défaut pour les versions antérieurs à Java 9, et comme Jackson est disponible pour Java 8, il est important de tester avec ce GC.
Également, valider le bon fonctionnement d'un GC est cruciale pour une application intense en mémoire comme Jackson.

## PrintGCDetails (type: Print)
Nous avons choisi "PrintGCDetails" afin d'obtenir des informations claires sur le fonctionnement du GC. Cela nous permet d'analyser l'impact des événements de GC sur les performances et donc de mieux comprendre comment la mémoire est gérée pendant les tests. Ces détails sont cruciaux pour identifier des problèmes futurs, par exemple ceux de latence. Étant donné que Jackon traite des données importantes, il est essentiel de surveiller la gestion de la mémoire pour éviter ce type dr problèmes, ce qui résultera en des performances plus optimales.

## UseAES (type: Security)
Nous nous sommes ensuite intéressés à l'aspect de la sécurité, puis nous avons trouvé "UseAES", qui est est un flag qui active l'utilisation des instructions AES pour les opérations cryptographiques. Cela permet en gros d'améliorer la vitesse des traitements sécurisés, ce qui ajoute une touche intéressante, selon nous, d'optimisation à l'aspect de sécurité. Pour les applications sensibles qui manipulent des données critiques (traitement sécurisé des objets JSON avec Jackson), ce flag assure que les opérations de chiffrement sont sûres et rapides, ce qui est évidemment primordial. De plus, en utilisant des instructions matérielles spécifiques, ce flag garantit un traitement encore plus optimisé des opérations de chiffrement, réduisant l'impact potentiel sur les performances globales.

## TieredCompilation (type: Compiler)
Finalement, nous avons considéré l'aspect de la compilation. En regardant la liste, nous avons trouvé "TieredCompilation", qui  est un flag qui active la compilation à plusieurs niveaux. Cela optimise les performances en compilant les sections critiques du code tout en laissant le reste interprété, offrant un équilibre intéressant entre la vitesse et l'efficacité. En utilisant une approche de la sorte, ce flag peut accélérer les tâches les plus importantes tout en minimisant l'empreinte mémoire globale. Ce comportement hybride est particulièrement utile pour Jackson, où certaines opérations peuvent nécessiter une optimisation plus poussée, tandis que d'autres peuvent rester légères, assurant ainsi un traitement fluide et efficace.
