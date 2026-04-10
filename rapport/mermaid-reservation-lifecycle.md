```mermaid
stateDiagram-v2
    [*] --> PENDING : Locataire reserve
    PENDING --> CONFIRMED : Owner confirme
    PENDING --> CANCELLED : Owner refuse
    CONFIRMED --> CANCELLATION_REQUESTED : Locataire demande annulation
    CANCELLATION_REQUESTED --> CANCELLED : Owner accepte
    CANCELLATION_REQUESTED --> CANCELLATION_REFUSED : Owner refuse
    CANCELLED --> [*]
    CANCELLATION_REFUSED --> [*]
```
