resource "aws_kms_key" "key" {
  description         = "KMS key used to encrypt data"
  enable_key_rotation = true
}

resource "aws_kms_alias" "alias" {
  name          = "alias/${var.service}-${var.environment}"
  target_key_id = aws_kms_key.key.key_id
}
