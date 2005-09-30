declare @annottypekey integer
select @annottypekey = _annottype_key from VOC_AnnotType where name = 'PIRSF/Marker'

declare @vocabkey integer
select @vocabkey = _vocab_key from voc_vocab where name = 'PIR Superfamily'

delete from voc_evidence
from voc_evidence e, voc_annot a
where a._annottype_key = @annottypekey
and a._annot_key = e._annot_key

delete from voc_annot
where _annottype_key = @annottypekey

delete from voc_term 
where _vocab_key = @vocabkey
