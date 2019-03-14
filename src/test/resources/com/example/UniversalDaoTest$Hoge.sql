hoge=
SELECT * FROM hoge
WHERE $if (baz) { baz = :baz }
