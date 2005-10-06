declare @annottypekey integer
select @annottypekey = _AnnotType_key from VOC_AnnotType where name = 'PIRSF/Marker'

declare @vocabkey integer
select @vocabkey = _Vocab_key from VOC_Vocab where name = 'PIR Superfamily'

delete from VOC_Evidence
from VOC_Evidence e, VOC_Annot a
where a._AnnotType_key = @annottypekey
and a._Annot_key = e._Annot_key

delete from VOC_Annot
where _AnnotType_key = @annottypekey

delete from VOC_Term 
where _Vocab_key = @vocabkey
