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