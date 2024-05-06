package TeamJ.MUSt.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Meaning {
    @Id
    @GeneratedValue
    @Column(name = "meaning_id")
    private Long id;

    private String meaning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    public Meaning(String meaning) {
        this.meaning = meaning;
    }

    public Meaning() {
    }
}
